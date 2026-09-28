package com.gearshare.handover.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "handover_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HandoverRecord {

    @Id
    private String rentalId;

    private Instant pickedUpAt;

    private Instant returnedAt;

    private boolean damaged;

    private BigDecimal deductionAmount;

    private String description;
}
