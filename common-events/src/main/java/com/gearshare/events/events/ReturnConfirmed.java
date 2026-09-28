package com.gearshare.events.events;

/**
 * Fulfillment saga (#2), step 2 of 2 (no-damage branch). Published by handover-service
 * when the item is returned with nothing to report. payment-service listens for this to
 * capture the full authorized amount.
 */
public record ReturnConfirmed(String rentalId) {
}
