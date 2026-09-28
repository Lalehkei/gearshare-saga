package com.gearshare.catalog.messaging.choreography;

import com.gearshare.catalog.service.CatalogSagaService;
import com.gearshare.catalog.service.HoldResult;
import com.gearshare.events.Topics;
import com.gearshare.events.events.ApprovalRejected;
import com.gearshare.events.events.ApprovalTimedOut;
import com.gearshare.events.events.AvailabilityHeld;
import com.gearshare.events.events.AvailabilityRejected;
import com.gearshare.events.events.PaymentAuthorizationFailed;
import com.gearshare.events.events.RentalCancelled;
import com.gearshare.events.events.RentalRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * CHOREOGRAPHY MODE ONLY (active when SPRING_PROFILES_ACTIVE=choreography).
 * <p>
 * catalog-service is saga #1's first step, and by convention in this project it is also
 * the participant that finally declares {@code RentalCancelled} once it has finished
 * unwinding -- see the README's "who declares the terminal event" note. No orchestrator
 * is involved: this class only knows about events, never about approval-service or
 * payment-service as services.
 */
@Component
@Profile("choreography")
@RequiredArgsConstructor
@Slf4j
public class CatalogEventListener {

    private final CatalogSagaService catalogSagaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = Topics.RENTAL_REQUESTED, groupId = "catalog-service")
    public void onRentalRequested(RentalRequested event) {
        log.info("[{}] received RentalRequested", event.rentalId());
        HoldResult result = catalogSagaService.holdAvailability(
                event.rentalId(), event.itemId(), event.startDate(), event.endDate());

        if (result.success()) {
            kafkaTemplate.send(Topics.AVAILABILITY_HELD, event.rentalId(),
                    new AvailabilityHeld(event.rentalId(), event.itemId(), event.ownerId(), event.totalPrice()));
        } else {
            // First step of the saga failed -- there is nothing upstream to compensate,
            // so this service can declare the whole saga cancelled immediately.
            kafkaTemplate.send(Topics.AVAILABILITY_REJECTED, event.rentalId(),
                    new AvailabilityRejected(event.rentalId(), event.itemId(), result.reason()));
            kafkaTemplate.send(Topics.RENTAL_CANCELLED, event.rentalId(),
                    new RentalCancelled(event.rentalId(), result.reason()));
        }
    }

    @KafkaListener(topics = Topics.APPROVAL_REJECTED, groupId = "catalog-service")
    public void onApprovalRejected(ApprovalRejected event) {
        compensateAndDeclareCancelled(event.rentalId(), event.reason());
    }

    @KafkaListener(topics = Topics.APPROVAL_TIMEDOUT, groupId = "catalog-service")
    public void onApprovalTimedOut(ApprovalTimedOut event) {
        compensateAndDeclareCancelled(event.rentalId(), "Owner did not respond to the approval request in time");
    }

    @KafkaListener(topics = Topics.PAYMENT_AUTHORIZATION_FAILED, groupId = "catalog-service")
    public void onPaymentAuthorizationFailed(PaymentAuthorizationFailed event) {
        compensateAndDeclareCancelled(event.rentalId(), event.reason());
    }

    private void compensateAndDeclareCancelled(String rentalId, String reason) {
        log.info("[{}] compensating: releasing availability hold ({})", rentalId, reason);
        catalogSagaService.releaseAvailability(rentalId);
        kafkaTemplate.send(Topics.RENTAL_CANCELLED, rentalId, new RentalCancelled(rentalId, reason));
    }
}
