package com.gearshare.events.commands;

/**
 * Orchestration-only. Every participant (catalog, approval, payment) replies with this
 * SAME record on its own reply topic ({@code catalog.replies}, {@code approval.replies},
 * {@code payment.replies}) after handling a command. The orchestrator's saga state
 * machine reads {@code success} to decide whether to issue the next command or start
 * compensating.
 */
public record CommandReply(String rentalId, boolean success, String reason) {

    public static CommandReply ok(String rentalId) {
        return new CommandReply(rentalId, true, null);
    }

    public static CommandReply fail(String rentalId, String reason) {
        return new CommandReply(rentalId, false, reason);
    }
}
