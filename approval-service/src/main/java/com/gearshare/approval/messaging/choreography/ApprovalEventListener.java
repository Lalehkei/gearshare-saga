package com.gearshare.approval.messaging.choreography;

import com.gearshare.approval.service.ApprovalSagaService;
import com.gearshare.events.Topics;
import com.gearshare.events.events.AvailabilityHeld;
import com.gearshare.events.events.PaymentAuthorizationFailed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * CHOREOGRAPHY MODE ONLY. Reacts to AvailabilityHeld (its trigger to ask the owner) and
 * to PaymentAuthorizationFailed (its cue to mark an already-granted approval MOOT).
 * Notice it does NOT publish anything in reaction to PaymentAuthorizationFailed --
 * catalog-service already took responsibility for declaring RentalCancelled, so this is
 * pure local bookkeeping. Nobody else needs to know.
 */
@Component
@Profile("choreography")
@RequiredArgsConstructor
@Slf4j
public class ApprovalEventListener {

    private final ApprovalSagaService approvalSagaService;

    @KafkaListener(topics = Topics.AVAILABILITY_HELD, groupId = "approval-service")
    public void onAvailabilityHeld(AvailabilityHeld event) {
        log.info("[{}] received AvailabilityHeld", event.rentalId());
        approvalSagaService.requestApproval(event.rentalId(), event.ownerId(), event.itemId(), event.totalPrice());
    }

    @KafkaListener(topics = Topics.PAYMENT_AUTHORIZATION_FAILED, groupId = "approval-service")
    public void onPaymentAuthorizationFailed(PaymentAuthorizationFailed event) {
        log.info("[{}] received PaymentAuthorizationFailed, marking approval moot", event.rentalId());
        approvalSagaService.markMoot(event.rentalId());
    }
}
