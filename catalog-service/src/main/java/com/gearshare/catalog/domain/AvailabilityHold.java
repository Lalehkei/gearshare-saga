package com.gearshare.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A record of the fact that a rental request holds a specific item for a date range.
 * The rentalId is the primary key: one hold per saga instance, which is what makes
 * {@code holdAvailability} and {@code releaseAvailability} safe to call more than once
 * (idempotent) if a message gets redelivered.
 */
@Entity
@Table(name = "availability_hold")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityHold {

    @Id
    private String rentalId;

    private String itemId;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private HoldStatus status;

    public enum HoldStatus {
        HELD,
        RELEASED
    }
}
