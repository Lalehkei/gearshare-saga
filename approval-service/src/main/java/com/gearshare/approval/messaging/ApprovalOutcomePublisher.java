package com.gearshare.approval.messaging;

import com.gearshare.approval.domain.ApprovalRequest;

/**
 * Abstracts away WHERE an approval decision gets announced. The business logic in
 * {@link com.gearshare.approval.service.ApprovalSagaService} never touches Kafka
 * directly; instead the REST controller and the timeout sweep depend on this interface,
 * and Spring wires in whichever implementation matches the active profile. This is what
 * lets the exact same decide()/timeout code paths work correctly in both orchestration
 * and choreography mode.
 */
public interface ApprovalOutcomePublisher {

    void granted(ApprovalRequest request);

    void rejected(ApprovalRequest request, String reason);

    void timedOut(ApprovalRequest request);
}
