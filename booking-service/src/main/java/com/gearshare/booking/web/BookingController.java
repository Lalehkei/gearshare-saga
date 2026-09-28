package com.gearshare.booking.web;

import com.gearshare.booking.domain.RentalRequestEntity;
import com.gearshare.booking.service.BookingService;
import com.gearshare.events.Topics;
import com.gearshare.events.events.RentalRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The system's one public entry point. POST here to start saga #1; poll GET to watch it
 * resolve. See the README for the exact item ids and the two "chaos" identifiers
 * (item-005, renterId "renter-blocked") that let you deterministically trigger every
 * failure path.
 */
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public record RentalRequestBody(
            String itemId, String renterId, String ownerId,
            LocalDate startDate, LocalDate endDate, BigDecimal totalPrice) {
    }

    @PostMapping
    public ResponseEntity<RentalRequestEntity> createRental(@RequestBody RentalRequestBody body) {
        RentalRequestEntity entity = bookingService.create(
                body.itemId(), body.renterId(), body.ownerId(), body.startDate(), body.endDate(), body.totalPrice());

        kafkaTemplate.send(Topics.RENTAL_REQUESTED, entity.getRentalId(), new RentalRequested(
                entity.getRentalId(), entity.getItemId(), entity.getRenterId(), entity.getOwnerId(),
                entity.getStartDate(), entity.getEndDate(), entity.getTotalPrice()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(entity);
    }

    @GetMapping("/{rentalId}")
    public ResponseEntity<RentalRequestEntity> get(@PathVariable String rentalId) {
        return bookingService.find(rentalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<RentalRequestEntity> list() {
        return bookingService.findAll();
    }
}
