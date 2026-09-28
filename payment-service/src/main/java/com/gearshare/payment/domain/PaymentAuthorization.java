package com.gearshare.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_authorization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorization {

    @Id
    private String rentalId;

    private String renterId;

    /** The amount held, NOT charged, at authorization time. */
    private BigDecimal authorizedAmount;

    private BigDecimal capturedAmount;

    private BigDecimal releasedAmount;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant createdAt;

    public enum Status {
        AUTHORIZED,
        RELEASED,
        CAPTURED,
        PARTIALLY_CAPTURED,
        FAILED
    }
}
