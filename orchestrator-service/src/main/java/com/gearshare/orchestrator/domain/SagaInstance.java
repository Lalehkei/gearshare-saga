package com.gearshare.orchestrator.domain;

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
 * The orchestrator's explicit view of where a saga instance is right now. This is the
 * central idea of orchestration: one place holds the whole saga's state, as opposed to
 * choreography where "the state of the saga" only exists implicitly, scattered across
 * whatever each participant privately remembers.
 */
@Entity
@Table(name = "saga_instance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SagaInstance {

    @Id
    private String rentalId;

    private String itemId;
    private String ownerId;
    private String renterId;
    private BigDecimal totalPrice;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private SagaState state;

    private String failureReason;

    private Instant createdAt;

    /** Set when we send RequestApprovalCommand; read by the saga-level timeout sweep. */
    private Instant approvalRequestedAt;

    public enum SagaState {
        /** Just started; HoldAvailabilityCommand sent, waiting on catalog.replies. */
        STARTED,
        /** RequestApprovalCommand sent, waiting on approval.replies (or the sweep). */
        AWAITING_APPROVAL,
        /** AuthorizePaymentCommand sent, waiting on payment.replies. */
        AWAITING_PAYMENT,
        /** Happy path finished: RentalConfirmed published. */
        COMPLETED,
        /** Failure path finished: compensations issued, RentalCancelled published. */
        CANCELLED
    }
}
