package com.gearshare.events.commands;

import java.math.BigDecimal;

/**
 * Orchestration-only. Sent by orchestrator-service to approval-service on
 * {@link com.gearshare.events.Topics#APPROVAL_REQUEST_CMD} as saga #1's second step,
 * once the availability hold succeeds.
 */
public record RequestApprovalCommand(String rentalId, String ownerId, String itemId, BigDecimal totalPrice) {
}
