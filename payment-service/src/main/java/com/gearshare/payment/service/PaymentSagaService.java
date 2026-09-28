package com.gearshare.payment.service;

import com.gearshare.payment.domain.PaymentAuthorization;
import com.gearshare.payment.domain.PaymentAuthorization.Status;
import com.gearshare.payment.domain.PaymentAuthorizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Deliberately keeps authorization (a HOLD) separate from capture (an actual CHARGE) --
 * exactly like a real card processor does when you book a hotel room. Saga #1 only ever
 * authorizes; saga #2 (the fulfillment flow, see {@code messaging.PaymentFulfillmentListener})
 * is what turns that hold into a real charge, in full or in part.
 * <p>
 * A renterId of "renter-blocked" always fails authorization -- a deterministic "chaos
 * hook" so you can reliably exercise the last-step-failure / full-compensation path
 * without depending on randomness. See the README's "things to try".
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaService {

    private static final String BLOCKED_RENTER = "renter-blocked";

    private final PaymentAuthorizationRepository repository;

    @Transactional
    public AuthorizationResult authorize(String rentalId, String renterId, BigDecimal amount) {
        Optional<PaymentAuthorization> existing = repository.findById(rentalId);
        if (existing.isPresent()) {
            PaymentAuthorization auth = existing.get();
            log.info("[{}] authorization already recorded as {}, treating redelivery as such", rentalId, auth.getStatus());
            return auth.getStatus() == Status.FAILED
                    ? AuthorizationResult.failure("Card declined for renter " + renterId)
                    : AuthorizationResult.ok();
        }

        if (BLOCKED_RENTER.equals(renterId)) {
            repository.save(new PaymentAuthorization(rentalId, renterId, amount, null, null, Status.FAILED, Instant.now()));
            log.info("[{}] authorization DECLINED for renter {}", rentalId, renterId);
            return AuthorizationResult.failure("Card declined for renter " + renterId);
        }

        repository.save(new PaymentAuthorization(rentalId, renterId, amount, null, null, Status.AUTHORIZED, Instant.now()));
        log.info("[{}] authorized a hold of {} for renter {}", rentalId, amount, renterId);
        return AuthorizationResult.ok();
    }

    /**
     * The compensating action for {@link #authorize}. Not exercised by saga #1 in this
     * project (payment authorization is the last step, so nothing after it can fail and
     * require releasing it) -- kept real and callable so you can see what a fourth saga
     * step's compensation would look like, and because the fulfillment saga's "no
     * damage" capture path uses the same idea (moving money out of a held state).
     */
    @Transactional
    public void release(String rentalId) {
        repository.findById(rentalId).ifPresent(auth -> {
            if (auth.getStatus() != Status.AUTHORIZED) {
                log.info("[{}] authorization is {}, not releasing (idempotent)", rentalId, auth.getStatus());
                return;
            }
            auth.setStatus(Status.RELEASED);
            auth.setReleasedAmount(auth.getAuthorizedAmount());
            repository.save(auth);
            log.info("[{}] released the authorization hold", rentalId);
        });
    }

    @Transactional
    public Optional<PaymentAuthorization> captureFull(String rentalId) {
        return repository.findById(rentalId).map(auth -> {
            if (auth.getStatus() != Status.AUTHORIZED) {
                log.info("[{}] cannot fully capture, authorization is {}", rentalId, auth.getStatus());
                return auth;
            }
            auth.setStatus(Status.CAPTURED);
            auth.setCapturedAmount(auth.getAuthorizedAmount());
            repository.save(auth);
            log.info("[{}] captured the full amount {}", rentalId, auth.getAuthorizedAmount());
            return auth;
        });
    }

    @Transactional
    public Optional<PaymentAuthorization> captureWithDamageDeduction(String rentalId, BigDecimal deductionAmount) {
        return repository.findById(rentalId).map(auth -> {
            if (auth.getStatus() != Status.AUTHORIZED) {
                log.info("[{}] cannot partially capture, authorization is {}", rentalId, auth.getStatus());
                return auth;
            }
            BigDecimal capped = deductionAmount.min(auth.getAuthorizedAmount());
            BigDecimal remainder = auth.getAuthorizedAmount().subtract(capped);
            auth.setStatus(Status.PARTIALLY_CAPTURED);
            auth.setCapturedAmount(capped);
            auth.setReleasedAmount(remainder);
            repository.save(auth);
            log.info("[{}] captured damage deduction {} and released remaining {}", rentalId, capped, remainder);
            return auth;
        });
    }

    public Optional<PaymentAuthorization> find(String rentalId) {
        return repository.findById(rentalId);
    }
}
