package com.gearshare.orchestrator.scheduling;

import com.gearshare.orchestrator.domain.SagaInstance;
import com.gearshare.orchestrator.domain.SagaInstanceRepository;
import com.gearshare.orchestrator.service.SagaOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * The saga-level safety net described in {@link SagaOrchestrationService#compensateUnresponsiveApproval}.
 * Deliberately set LONGER than approval-service's own {@code approval.timeout-seconds}
 * (60s by default) so that, in the normal case, approval-service's own timeout always
 * fires first and this sweep finds nothing. It only ever catches sagas where
 * approval-service failed to reply at all.
 */
@Component
@RequiredArgsConstructor
public class SagaTimeoutSweeper {

    private final SagaInstanceRepository repository;
    private final SagaOrchestrationService sagaOrchestrationService;

    @Value("${orchestrator.saga-timeout-seconds:90}")
    private long sagaTimeoutSeconds;

    @Scheduled(fixedDelayString = "${orchestrator.sweep-interval-ms:15000}")
    public void sweepUnresponsiveSagas() {
        Instant cutoff = Instant.now().minus(Duration.ofSeconds(sagaTimeoutSeconds));
        List<SagaInstance> stale = repository.findByStateAndApprovalRequestedAtBefore(
                SagaInstance.SagaState.AWAITING_APPROVAL, cutoff);
        for (SagaInstance saga : stale) {
            sagaOrchestrationService.compensateUnresponsiveApproval(saga);
        }
    }
}
