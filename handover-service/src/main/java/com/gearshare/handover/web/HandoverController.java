package com.gearshare.handover.web;

import com.gearshare.events.Topics;
import com.gearshare.events.events.DamageClaimFiled;
import com.gearshare.events.events.PickupConfirmed;
import com.gearshare.events.events.ReturnConfirmed;
import com.gearshare.handover.domain.HandoverRecord;
import com.gearshare.handover.service.HandoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Triggers saga #2 (fulfillment). This is the only place in the whole project that
 * plays the role of "the two humans meeting up in person" -- call /pickup when the
 * renter collects the item, then /return when they bring it back. A damaged return
 * kicks off a partial capture in payment-service instead of a full one.
 */
@RestController
@RequestMapping("/api/handovers")
@RequiredArgsConstructor
public class HandoverController {

    private final HandoverService handoverService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/{rentalId}")
    public ResponseEntity<HandoverRecord> get(@PathVariable String rentalId) {
        return handoverService.find(rentalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{rentalId}/pickup")
    public HandoverRecord pickup(@PathVariable String rentalId) {
        HandoverRecord record = handoverService.confirmPickup(rentalId);
        kafkaTemplate.send(Topics.PICKUP_CONFIRMED, rentalId, new PickupConfirmed(rentalId));
        return record;
    }

    public record ReturnRequest(boolean damaged, BigDecimal deductionAmount, String description) {
    }

    @PostMapping("/{rentalId}/return")
    public HandoverRecord returnItem(@PathVariable String rentalId, @RequestBody ReturnRequest body) {
        HandoverRecord record = handoverService.confirmReturn(rentalId, body.damaged(), body.deductionAmount(), body.description());
        if (body.damaged()) {
            kafkaTemplate.send(Topics.DAMAGE_CLAIM_FILED, rentalId,
                    new DamageClaimFiled(rentalId, body.deductionAmount(), body.description()));
        } else {
            kafkaTemplate.send(Topics.RETURN_CONFIRMED, rentalId, new ReturnConfirmed(rentalId));
        }
        return record;
    }
}
