package com.gearshare.events.events;

/**
 * Choreography-only. Published by approval-service's scheduled sweep when an owner
 * doesn't respond within the approval window. This is the project's concrete example
 * of a TIME-triggered compensation, as opposed to a failure-triggered one: nothing
 * "went wrong" technically, a human just didn't answer in time, and the saga still has
 * to unwind cleanly.
 */
public record ApprovalTimedOut(String rentalId, String itemId) {
}
