package com.gearshare.approval.messaging.choreography;

import com.gearshare.approval.domain.ApprovalRequest;
import com.gearshare.approval.messaging.ApprovalOutcomePublisher;
import com.gearshare.events.Topics;
import com.gearshare.events.events.ApprovalGranted;
import com.gearshare.events.events.ApprovalRejected;
import com.gearshare.events.events.ApprovalTimedOut;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** CHOREOGRAPHY MODE ONLY. Each outcome becomes its own distinct domain event. */
@Component
@Profile("choreography")
@RequiredArgsConstructor
public class ChoreographyApprovalOutcomePublisher implements ApprovalOutcomePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void granted(ApprovalRequest request) {
        kafkaTemplate.send(Topics.APPROVAL_GRANTED, request.getRentalId(),
                new ApprovalGranted(request.getRentalId(), request.getOwnerId(), request.getTotalPrice()));
    }

    @Override
    public void rejected(ApprovalRequest request, String reason) {
        kafkaTemplate.send(Topics.APPROVAL_REJECTED, request.getRentalId(),
                new ApprovalRejected(request.getRentalId(), request.getItemId(), reason));
    }

    @Override
    public void timedOut(ApprovalRequest request) {
        kafkaTemplate.send(Topics.APPROVAL_TIMEDOUT, request.getRentalId(),
                new ApprovalTimedOut(request.getRentalId(), request.getItemId()));
    }
}
