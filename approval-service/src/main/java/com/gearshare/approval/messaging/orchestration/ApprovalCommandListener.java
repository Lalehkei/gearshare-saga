package com.gearshare.approval.messaging.orchestration;

import com.gearshare.approval.service.ApprovalSagaService;
import com.gearshare.events.Topics;
import com.gearshare.events.commands.CancelApprovalCommand;
import com.gearshare.events.commands.RequestApprovalCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ORCHESTRATION MODE ONLY. Note there's no reply sent from {@link #onRequest}: asking
 * for approval doesn't have an immediate answer. The reply comes later, from whichever
 * of {@link com.gearshare.approval.web.ApprovalController} or
 * {@link com.gearshare.approval.scheduling.ApprovalTimeoutSweeper} resolves the request.
 */
@Component
@Profile("orchestration")
@RequiredArgsConstructor
@Slf4j
public class ApprovalCommandListener {

    private final ApprovalSagaService approvalSagaService;

    @KafkaListener(topics = Topics.APPROVAL_REQUEST_CMD, groupId = "approval-service")
    public void onRequest(RequestApprovalCommand command) {
        log.info("[{}] received RequestApprovalCommand", command.rentalId());
        approvalSagaService.requestApproval(command.rentalId(), command.ownerId(), command.itemId(), command.totalPrice());
    }

    @KafkaListener(topics = Topics.APPROVAL_CANCEL_CMD, groupId = "approval-service")
    public void onCancel(CancelApprovalCommand command) {
        log.info("[{}] received CancelApprovalCommand (compensation)", command.rentalId());
        approvalSagaService.markMoot(command.rentalId());
    }
}
