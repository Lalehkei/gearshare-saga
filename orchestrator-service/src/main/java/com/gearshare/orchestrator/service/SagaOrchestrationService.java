package com.gearshare.orchestrator.service;

import com.gearshare.events.Topics;
import com.gearshare.events.commands.AuthorizePaymentCommand;
import com.gearshare.events.commands.CancelApprovalCommand;
import com.gearshare.events.commands.HoldAvailabilityCommand;
import com.gearshare.events.commands.ReleaseAvailabilityCommand;
import com.gearshare.events.commands.RequestApprovalCommand;
import com.gearshare.events.events.RentalCancelled;
import com.gearshare.events.events.RentalConfirmed;
import com.gearshare.events.events.RentalRequested;
import com.gearshare.orchestrator.domain.SagaInstance;
import com.gearshare.orchestrator.domain.SagaInstance.SagaState;
import com.gearshare.orchestrator.domain.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * The explicit saga #1 state machine. Every method here does the same two things a
 * choreographed participant does implicitly and separately (act, then decide what
 * happens next) but does them BOTH, for EVERY participant, in one central place. That
 * centralization is orchestration's whole trade-off: easier to trace end-to-end (this
 * class IS the saga, readable top to bottom) at the cost of the orchestrator becoming a
 * dependency every participant indirectly relies on.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrationService {

    private final SagaInstanceRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void start(RentalRequested event) {
        if (repository.existsById(event.rentalId())) {
            log.info("[{}] saga already started, ignoring redelivery", event.rentalId());
            return;
        }
        SagaInstance saga = new SagaInstance(
                event.rentalId(), event.itemId(), event.ownerId(), event.renterId(), event.totalPrice(),
                event.startDate(), event.endDate(), SagaState.STARTED, null, Instant.now(), null);
        repository.save(saga);

        log.info("[{}] saga STARTED -- sending HoldAvailabilityCommand", event.rentalId());
        kafkaTemplate.send(Topics.CATALOG_HOLD_CMD, event.rentalId(),
                new HoldAvailabilityCommand(event.rentalId(), event.itemId(), event.startDate(), event.endDate()));
    }

    @Transactional
    public void onCatalogReply(String rentalId, boolean success, String reason) {
        SagaInstance saga = requireInState(rentalId, SagaState.STARTED, "CATALOG_REPLY");
        if (saga == null) {
            return;
        }

        if (success) {
            log.info("[{}] availability held -- sending RequestApprovalCommand", rentalId);
            saga.setState(SagaState.AWAITING_APPROVAL);
            saga.setApprovalRequestedAt(Instant.now());
            repository.save(saga);
            kafkaTemplate.send(Topics.APPROVAL_REQUEST_CMD, rentalId,
                    new RequestApprovalCommand(rentalId, saga.getOwnerId(), saga.getItemId(), saga.getTotalPrice()));
        } else {
            // First step failed: nothing succeeded yet, so there is nothing to compensate.
            cancel(saga, reason);
        }
    }

    @Transactional
    public void onApprovalReply(String rentalId, boolean success, String reason) {
        SagaInstance saga = requireInState(rentalId, SagaState.AWAITING_APPROVAL, "APPROVAL_REPLY");
        if (saga == null) {
            return;
        }

        if (success) {
            log.info("[{}] owner approved -- sending AuthorizePaymentCommand", rentalId);
            saga.setState(SagaState.AWAITING_PAYMENT);
            repository.save(saga);
            kafkaTemplate.send(Topics.PAYMENT_AUTHORIZE_CMD, rentalId,
                    new AuthorizePaymentCommand(rentalId, saga.getRenterId(), saga.getTotalPrice()));
        } else {
            log.info("[{}] approval failed ({}) -- compensating catalog hold", rentalId, reason);
            kafkaTemplate.send(Topics.CATALOG_RELEASE_CMD, rentalId,
                    new ReleaseAvailabilityCommand(rentalId, saga.getItemId()));
            cancel(saga, reason);
        }
    }

    @Transactional
    public void onPaymentReply(String rentalId, boolean success, String reason) {
        SagaInstance saga = requireInState(rentalId, SagaState.AWAITING_PAYMENT, "PAYMENT_REPLY");
        if (saga == null) {
            return;
        }

        if (success) {
            log.info("[{}] payment authorized -- saga COMPLETED", rentalId);
            saga.setState(SagaState.COMPLETED);
            repository.save(saga);
            kafkaTemplate.send(Topics.RENTAL_CONFIRMED, rentalId, new RentalConfirmed(rentalId));
        } else {
            // Last step failed: walk backward through every step that DID succeed.
            // approval-service's CancelApprovalCommand handler is a no-op here (the
            // request was already GRANTED) -- we send it anyway, uniformly, because a
            // real orchestrator doesn't special-case "did this particular compensation
            // actually have anything to undo"; compensations are expected to be
            // idempotent and safe to call regardless.
            log.info("[{}] payment failed ({}) -- compensating approval and catalog", rentalId, reason);
            kafkaTemplate.send(Topics.APPROVAL_CANCEL_CMD, rentalId, new CancelApprovalCommand(rentalId));
            kafkaTemplate.send(Topics.CATALOG_RELEASE_CMD, rentalId,
                    new ReleaseAvailabilityCommand(rentalId, saga.getItemId()));
            cancel(saga, reason);
        }
    }

    /**
     * The saga-level timeout backstop, invoked by the scheduled sweep. Distinct from
     * approval-service's OWN 60s timeout: that one handles "the owner didn't answer."
     * This one (a longer window) handles "approval-service itself never replied at all"
     * -- e.g. the container is down. Try killing approval-service mid-saga to see this
     * fire instead of the normal per-request timeout.
     */
    @Transactional
    public void compensateUnresponsiveApproval(SagaInstance saga) {
        log.warn("[{}] no reply from approval-service within the saga-level backstop window -- " +
                "compensating as if it failed", saga.getRentalId());
        kafkaTemplate.send(Topics.APPROVAL_CANCEL_CMD, saga.getRentalId(), new CancelApprovalCommand(saga.getRentalId()));
        kafkaTemplate.send(Topics.CATALOG_RELEASE_CMD, saga.getRentalId(),
                new ReleaseAvailabilityCommand(saga.getRentalId(), saga.getItemId()));
        cancel(saga, "approval-service did not respond in time (service unavailable?)");
    }

    private void cancel(SagaInstance saga, String reason) {
        saga.setState(SagaState.CANCELLED);
        saga.setFailureReason(reason);
        repository.save(saga);
        kafkaTemplate.send(Topics.RENTAL_CANCELLED, saga.getRentalId(), new RentalCancelled(saga.getRentalId(), reason));
    }

    private SagaInstance requireInState(String rentalId, SagaState expected, String replyKind) {
        Optional<SagaInstance> found = repository.findById(rentalId);
        if (found.isEmpty()) {
            log.warn("[{}] received {} for unknown saga, ignoring", rentalId, replyKind);
            return null;
        }
        SagaInstance saga = found.get();
        if (saga.getState() != expected) {
            // Expected if a reply arrives late, after the saga-level backstop already
            // compensated it -- exactly the race the timeout sweep exists to resolve
            // gracefully instead of double-processing.
            log.warn("[{}] received {} but saga is in state {} (expected {}), ignoring",
                    rentalId, replyKind, saga.getState(), expected);
            return null;
        }
        return saga;
    }
}
