package com.gearshare.orchestrator.messaging;

import com.gearshare.events.Topics;
import com.gearshare.events.commands.CommandReply;
import com.gearshare.events.events.RentalRequested;
import com.gearshare.orchestrator.service.SagaOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Thin translation layer: one @KafkaListener per topic, each just forwarding into
 * {@link SagaOrchestrationService}. All the actual decision-making lives there, not
 * here, so it can be read as one linear state machine instead of being scattered across
 * listener methods.
 */
@Component
@RequiredArgsConstructor
public class OrchestratorListener {

    private final SagaOrchestrationService sagaOrchestrationService;

    @KafkaListener(topics = Topics.RENTAL_REQUESTED, groupId = "orchestrator-service")
    public void onRentalRequested(RentalRequested event) {
        sagaOrchestrationService.start(event);
    }

    @KafkaListener(topics = Topics.CATALOG_REPLY, groupId = "orchestrator-service")
    public void onCatalogReply(CommandReply reply) {
        sagaOrchestrationService.onCatalogReply(reply.rentalId(), reply.success(), reply.reason());
    }

    @KafkaListener(topics = Topics.APPROVAL_REPLY, groupId = "orchestrator-service")
    public void onApprovalReply(CommandReply reply) {
        sagaOrchestrationService.onApprovalReply(reply.rentalId(), reply.success(), reply.reason());
    }

    @KafkaListener(topics = Topics.PAYMENT_REPLY, groupId = "orchestrator-service")
    public void onPaymentReply(CommandReply reply) {
        sagaOrchestrationService.onPaymentReply(reply.rentalId(), reply.success(), reply.reason());
    }
}
