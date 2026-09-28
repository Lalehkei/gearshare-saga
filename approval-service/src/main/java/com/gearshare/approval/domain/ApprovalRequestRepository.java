package com.gearshare.approval.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {

    List<ApprovalRequest> findByStatusAndRequestedAtBefore(ApprovalRequest.Status status, Instant cutoff);
}
