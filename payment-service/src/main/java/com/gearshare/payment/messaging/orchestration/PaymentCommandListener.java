package com.gearshare.payment.messaging.orchestration;

import com.gearshare.events.Topics;
import com.gearshare.events.commands.AuthorizePaymentCommand;
import com.gearshare.events.commands.CommandReply;
import com.gearshare.events.commands.ReleasePaymentCommand;
import com.gearshare.payment.service.AuthorizationResult;
import com.gearshare.payment.service.PaymentSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * ORCHESTRATION MODE ONLY. Replies to the orchestrator and stops there -- it is the
 * orchestrator, not payment-service, that decides the saga is complete and publishes
 * RentalConfirmed. Compare {@link com.gearshare.payment.messaging.choreography.PaymentEventListener},
 * which -- having no coordinator to report to -- must make that call itself.
 */
@Component
@Profile("orchestration")
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandListener {

    private final PaymentSagaService paymentSagaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = Topics.PAYMENT_AUTHORIZE_CMD, groupId = "payment-service")
    public void onAuthorize(AuthorizePaymentCommand command) {
        log.info("[{}] received AuthorizePaymentCommand", command.rentalId());
        AuthorizationResult result = paymentSagaService.authorize(command.rentalId(), command.renterId(), command.amount());

        CommandReply reply = result.success()
                ? CommandReply.ok(command.rentalId())
                : CommandReply.fail(command.rentalId(), result.reason());
        kafkaTemplate.send(Topics.PAYMENT_REPLY, command.rentalId(), reply);
    }

    @KafkaListener(topics = Topics.PAYMENT_RELEASE_CMD, groupId = "payment-service")
    public void onRelease(ReleasePaymentCommand command) {
        log.info("[{}] received ReleasePaymentCommand (compensation)", command.rentalId());
        paymentSagaService.release(command.rentalId());
    }
}
