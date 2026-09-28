package com.gearshare.approval.messaging.orchestration;

import com.gearshare.approval.domain.ApprovalRequest;
import com.gearshare.approval.messaging.ApprovalOutcomePublisher;
import com.gearshare.events.Topics;
import com.gearshare.events.commands.CommandReply;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** ORCHESTRATION MODE ONLY. Every outcome becomes one CommandReply on the reply topic. */
@Component
@Profile("orchestration")
@RequiredArgsConstructor
public class OrchestrationApprovalOutcomePublisher implements ApprovalOutcomePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void granted(ApprovalRequest request) {
        kafkaTemplate.send(Topics.APPROVAL_REPLY, request.getRentalId(), CommandReply.ok(request.getRentalId()));
    }

    @Override
    public void rejected(ApprovalRequest request, String reason) {
        kafkaTemplate.send(Topics.APPROVAL_REPLY, request.getRentalId(), CommandReply.fail(request.getRentalId(), reason));
    }

    @Override
    public void timedOut(ApprovalRequest request) {
        kafkaTemplate.send(Topics.APPROVAL_REPLY, request.getRentalId(),
                CommandReply.fail(request.getRentalId(), "Owner did not respond in time"));
    }
}
