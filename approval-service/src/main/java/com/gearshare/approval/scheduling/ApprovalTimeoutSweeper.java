package com.gearshare.approval.scheduling;

import com.gearshare.approval.domain.ApprovalRequest;
import com.gearshare.approval.domain.ApprovalRequestRepository;
import com.gearshare.approval.messaging.ApprovalOutcomePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * The concrete example of a TIME-triggered compensation: nobody has to fail anything --
 * a human (the simulated gear owner) just has to not answer. This runs identically
 * regardless of coordination style; only {@link ApprovalOutcomePublisher} differs.
 * <p>
 * Try it: POST a rental and then simply don't call the decision endpoint. After
 * {@code approval.timeout-seconds} this sweep marks it TIMED_OUT and the saga unwinds.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApprovalTimeoutSweeper {

    private final ApprovalRequestRepository repository;
    private final ApprovalOutcomePublisher outcomePublisher;

    @Value("${approval.timeout-seconds:60}")
    private long timeoutSeconds;

    @Scheduled(fixedDelayString = "${approval.sweep-interval-ms:10000}")
    @Transactional
    public void sweepTimedOutRequests() {
        Instant cutoff = Instant.now().minus(Duration.ofSeconds(timeoutSeconds));
        List<ApprovalRequest> stale = repository.findByStatusAndRequestedAtBefore(ApprovalRequest.Status.PENDING, cutoff);
        for (ApprovalRequest request : stale) {
            request.setStatus(ApprovalRequest.Status.TIMED_OUT);
            repository.save(request);
            log.info("[{}] approval timed out after {}s with no owner response", request.getRentalId(), timeoutSeconds);
            outcomePublisher.timedOut(request);
        }
    }
}
