package com.gearshare.events.events;

import java.math.BigDecimal;

/**
 * Fulfillment saga (#2), step 2 of 2 (damage branch). Published by handover-service
 * instead of ReturnConfirmed when the owner reports damage on return. payment-service
 * listens for this to capture only the deduction amount and release the rest of the
 * hold back to the renter -- a partial compensation, not a full one.
 */
public record DamageClaimFiled(String rentalId, BigDecimal deductionAmount, String description) {
}
