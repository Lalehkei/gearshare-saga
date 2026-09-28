package com.gearshare.approval.service;

import com.gearshare.approval.domain.ApprovalRequest;
import com.gearshare.approval.domain.ApprovalRequest.Status;
import com.gearshare.approval.domain.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Business logic only -- publishing the outcome to Kafka is deliberately NOT this
 * class's job. See {@link com.gearshare.approval.messaging.ApprovalOutcomePublisher}:
 * requesting approval doesn't produce an immediate outcome (a human has to act, or time
 * has to pass), so "record the request" and "announce the decision" happen in different
 * places at different times, unlike catalog-service's hold/release which both finish
 * synchronously within one method call.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalSagaService {

    private final ApprovalRequestRepository repository;

    @Transactional
    public void requestApproval(String rentalId, String ownerId, String itemId, BigDecimal totalPrice) {
        if (repository.existsById(rentalId)) {
            log.info("[{}] approval request already recorded, ignoring redelivery", rentalId);
            return;
        }
        ApprovalRequest request = new ApprovalRequest(rentalId, itemId, ownerId, totalPrice, Status.PENDING, Instant.now());
        repository.save(request);
        log.info("[{}] recorded pending approval request for owner {}", rentalId, ownerId);
    }

    /**
     * @return the request in its new state, or empty if there was nothing to decide
     *         (unknown rentalId, or it already left the PENDING state -- e.g. the
     *         timeout sweep beat the owner to it).
     */
    @Transactional
    public Optional<ApprovalRequest> decide(String rentalId, boolean approved) {
        Optional<ApprovalRequest> found = repository.findById(rentalId);
        if (found.isEmpty()) {
            log.info("[{}] decision received for unknown rental, ignoring", rentalId);
            return Optional.empty();
        }
        ApprovalRequest request = found.get();
        if (request.getStatus() != Status.PENDING) {
            log.info("[{}] decision received but request is already {}, ignoring", rentalId, request.getStatus());
            return Optional.empty();
        }
        request.setStatus(approved ? Status.GRANTED : Status.REJECTED);
        repository.save(request);
        log.info("[{}] owner {} the request", rentalId, approved ? "GRANTED" : "REJECTED");
        return Optional.of(request);
    }

    @Transactional
    public void markMoot(String rentalId) {
        repository.findById(rentalId).ifPresent(request -> {
            if (request.getStatus() == Status.PENDING || request.getStatus() == Status.GRANTED) {
                request.setStatus(Status.MOOT);
                repository.save(request);
                log.info("[{}] marked approval MOOT (a later step failed first)", rentalId);
            }
        });
    }

    public Optional<ApprovalRequest> find(String rentalId) {
        return repository.findById(rentalId);
    }
}
