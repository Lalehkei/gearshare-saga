package com.gearshare.events.commands;

import java.time.LocalDate;

/**
 * Orchestration-only. Sent by orchestrator-service to catalog-service on
 * {@link com.gearshare.events.Topics#CATALOG_HOLD_CMD} as saga #1's first step.
 */
public record HoldAvailabilityCommand(String rentalId, String itemId, LocalDate startDate, LocalDate endDate) {
}
