package com.gearshare.events.commands;

/**
 * Orchestration-only. The compensating command for {@link RequestApprovalCommand},
 * sent on {@link com.gearshare.events.Topics#APPROVAL_CANCEL_CMD}. Marks a still-pending
 * approval request as moot so a late owner response is ignored.
 */
public record CancelApprovalCommand(String rentalId) {
}
