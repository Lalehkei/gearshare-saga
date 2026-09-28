package com.gearshare.approval.domain;

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
@Table(name = "approval_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @Id
    private String rentalId;

    private String itemId;

    private String ownerId;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant requestedAt;

    public enum Status {
        PENDING,
        GRANTED,
        REJECTED,
        TIMED_OUT,
        /** Owner never got a chance to answer -- something downstream failed first. */
        MOOT
    }
}
