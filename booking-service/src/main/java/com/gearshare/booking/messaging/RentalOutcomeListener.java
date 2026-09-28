package com.gearshare.booking.messaging;

import com.gearshare.booking.service.BookingService;
import com.gearshare.events.Topics;
import com.gearshare.events.events.RentalCancelled;
import com.gearshare.events.events.RentalConfirmed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * NO @Profile ANNOTATION -- booking-service's read model updates the same way no matter
 * which coordination style produced the outcome. In orchestration mode these two events
 * come from orchestrator-service; in choreography mode they come from catalog-service
 * (cancellation) or payment-service (confirmation). booking-service doesn't need to know
 * or care which.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RentalOutcomeListener {

    private final BookingService bookingService;

    @KafkaListener(topics = Topics.RENTAL_CONFIRMED, groupId = "booking-service")
    public void onConfirmed(RentalConfirmed event) {
        bookingService.markConfirmed(event.rentalId());
    }

    @KafkaListener(topics = Topics.RENTAL_CANCELLED, groupId = "booking-service")
    public void onCancelled(RentalCancelled event) {
        bookingService.markCancelled(event.rentalId(), event.reason());
    }
}
