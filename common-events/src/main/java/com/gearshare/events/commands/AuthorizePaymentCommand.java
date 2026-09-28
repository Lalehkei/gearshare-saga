package com.gearshare.events.commands;

import java.math.BigDecimal;

/**
 * Orchestration-only. Sent by orchestrator-service to payment-service on
 * {@link com.gearshare.events.Topics#PAYMENT_AUTHORIZE_CMD} as saga #1's third and
 * final step, once the owner approves.
 */
public record AuthorizePaymentCommand(String rentalId, String renterId, BigDecimal amount) {
}
