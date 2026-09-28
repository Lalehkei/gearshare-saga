package com.gearshare.payment.messaging.choreography;

import com.gearshare.events.Topics;
import com.gearshare.events.events.ApprovalGranted;
import com.gearshare.events.events.PaymentAuthorizationFailed;
import com.gearshare.events.events.PaymentAuthorized;
import com.gearshare.events.events.RentalConfirmed;
import com.gearshare.payment.service.AuthorizationResult;
import com.gearshare.payment.service.PaymentSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * CHOREOGRAPHY MODE ONLY. payment-service is saga #1's last step and, in choreography,
 * the last step is responsible for declaring the happy-path outcome -- there's no
 * orchestrator to do it centrally. On failure it publishes PaymentAuthorizationFailed
 * and stops: catalog-service and approval-service both listen for that event
 * independently to compensate themselves, and catalog-service is the one that then
 * declares RentalCancelled. This service never talks to those two directly.
 */
@Component
@Profile("choreography")
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentSagaService paymentSagaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = Topics.APPROVAL_GRANTED, groupId = "payment-service")
    public void onApprovalGranted(ApprovalGranted event) {
        log.info("[{}] received ApprovalGranted", event.rentalId());
        AuthorizationResult result = paymentSagaService.authorize(event.rentalId(), event.renterId(), event.totalPrice());

        if (result.success()) {
            kafkaTemplate.send(Topics.PAYMENT_AUTHORIZED, event.rentalId(), new PaymentAuthorized(event.rentalId()));
            kafkaTemplate.send(Topics.RENTAL_CONFIRMED, event.rentalId(), new RentalConfirmed(event.rentalId()));
        } else {
            kafkaTemplate.send(Topics.PAYMENT_AUTHORIZATION_FAILED, event.rentalId(),
                    new PaymentAuthorizationFailed(event.rentalId(), result.reason()));
        }
    }
}
