package com.gearshare.payment.messaging;

import com.gearshare.events.Topics;
import com.gearshare.events.events.DamageClaimFiled;
import com.gearshare.events.events.PaymentCaptured;
import com.gearshare.events.events.PaymentPartiallyCaptured;
import com.gearshare.events.events.ReturnConfirmed;
import com.gearshare.payment.domain.PaymentAuthorization;
import com.gearshare.payment.service.PaymentSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * NO @Profile ANNOTATION -- deliberately active in BOTH orchestration and choreography
 * mode. Saga #2 (the fulfillment flow, after the rental is already confirmed) is a
 * short, linear, two-branch chain, and it is choreographed the same way regardless of
 * which style coordinated saga #1: handover-service reports what happened, and
 * payment-service reacts. See the README for why we didn't bother building an
 * orchestrated version of this second saga -- the payoff wouldn't be worth the
 * duplication for something this simple.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFulfillmentListener {

    private final PaymentSagaService paymentSagaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = Topics.RETURN_CONFIRMED, groupId = "payment-service")
    public void onReturnConfirmed(ReturnConfirmed event) {
        log.info("[{}] received ReturnConfirmed, capturing full amount", event.rentalId());
        paymentSagaService.captureFull(event.rentalId()).ifPresent(auth ->
                kafkaTemplate.send(Topics.PAYMENT_CAPTURED, event.rentalId(),
                        new PaymentCaptured(event.rentalId(), auth.getCapturedAmount())));
    }

    @KafkaListener(topics = Topics.DAMAGE_CLAIM_FILED, groupId = "payment-service")
    public void onDamageClaimFiled(DamageClaimFiled event) {
        log.info("[{}] received DamageClaimFiled, capturing deduction of {}", event.rentalId(), event.deductionAmount());
        paymentSagaService.captureWithDamageDeduction(event.rentalId(), event.deductionAmount())
                .ifPresent((PaymentAuthorization auth) -> kafkaTemplate.send(Topics.PAYMENT_PARTIALLY_CAPTURED, event.rentalId(),
                        new PaymentPartiallyCaptured(event.rentalId(), auth.getCapturedAmount(), auth.getReleasedAmount())));
    }
}
