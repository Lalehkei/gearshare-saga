package com.gearshare.events.commands;

/**
 * Orchestration-only. The compensating command for {@link HoldAvailabilityCommand},
 * sent by orchestrator-service on {@link com.gearshare.events.Topics#CATALOG_RELEASE_CMD}
 * whenever a later step in saga #1 fails.
 */
public record ReleaseAvailabilityCommand(String rentalId, String itemId) {
}
