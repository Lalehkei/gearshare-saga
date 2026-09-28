package com.gearshare.orchestrator.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, String> {

    List<SagaInstance> findByStateAndApprovalRequestedAtBefore(SagaInstance.SagaState state, Instant cutoff);
}
