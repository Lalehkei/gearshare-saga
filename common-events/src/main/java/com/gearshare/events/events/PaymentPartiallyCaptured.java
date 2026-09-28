package com.gearshare.events.events;

import java.math.BigDecimal;

/**
 * Fulfillment saga (#2) terminal event, damage branch. Published by payment-service
 * after capturing the damage deduction and releasing the remainder of the hold.
 */
public record PaymentPartiallyCaptured(String rentalId, BigDecimal capturedAmount, BigDecimal releasedAmount) {
}
