package com.gearshare.events.events;

/**
 * Fulfillment saga (#2), step 1 of 2. Published by handover-service when the renter
 * is recorded as having picked up the item. Purely informational in this project --
 * it exists so the fulfillment timeline is complete and easy to extend.
 */
public record PickupConfirmed(String rentalId) {
}
