package com.gearshare.booking.domain;

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
import java.time.LocalDate;

/**
 * booking-service's read model for a rental request. This is deliberately named
 * RentalRequestEntity (not RentalRequest) to avoid clashing with the common-events
 * record of the same concept -- the two are NOT the same thing: this one is a mutable,
 * locally-owned row that tracks the saga's outcome over time, while RentalRequested is
 * a one-shot, immutable message.
 */
@Entity
@Table(name = "rental_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequestEntity {

    @Id
    private String rentalId;

    private String itemId;

    private String renterId;

    private String ownerId;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String reason;

    private Instant createdAt;

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED
    }
}
