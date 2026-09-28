package com.gearshare.approval.web;

import com.gearshare.approval.domain.ApprovalRequest;
import com.gearshare.approval.messaging.ApprovalOutcomePublisher;
import com.gearshare.approval.service.ApprovalSagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Stands in for the gear owner's mobile app. There is no real owner to notify in this
 * project, so YOU play the owner: call POST .../decision to approve or decline, or do
 * nothing and let {@link com.gearshare.approval.scheduling.ApprovalTimeoutSweeper} time
 * it out.
 */
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalSagaService approvalSagaService;
    private final ApprovalOutcomePublisher outcomePublisher;

    @GetMapping("/{rentalId}")
    public ResponseEntity<ApprovalRequest> get(@PathVariable String rentalId) {
        return approvalSagaService.find(rentalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record DecisionRequest(boolean approved) {
    }

    @PostMapping("/{rentalId}/decision")
    public ResponseEntity<ApprovalRequest> decide(@PathVariable String rentalId, @RequestBody DecisionRequest body) {
        Optional<ApprovalRequest> updated = approvalSagaService.decide(rentalId, body.approved());
        if (updated.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ApprovalRequest request = updated.get();
        if (body.approved()) {
            outcomePublisher.granted(request);
        } else {
            outcomePublisher.rejected(request, "Owner declined the request");
        }
        return ResponseEntity.ok(request);
    }
}
