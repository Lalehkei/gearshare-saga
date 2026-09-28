package com.gearshare.catalog.service;

import com.gearshare.catalog.domain.AvailabilityHold;
import com.gearshare.catalog.domain.AvailabilityHold.HoldStatus;
import com.gearshare.catalog.domain.AvailabilityHoldRepository;
import com.gearshare.catalog.domain.GearItem;
import com.gearshare.catalog.domain.GearItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * The one piece of business logic in this service. Both the orchestration-mode command
 * listener and the choreography-mode event listener call these two methods -- neither
 * knows or cares which coordination style triggered it. That's the point: the saga
 * pattern changes how a step is TRIGGERED and how its outcome is REPORTED, not what the
 * step itself does.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSagaService {

    private final GearItemRepository itemRepository;
    private final AvailabilityHoldRepository holdRepository;

    @Transactional
    public HoldResult holdAvailability(String rentalId, String itemId, LocalDate startDate, LocalDate endDate) {
        if (holdRepository.existsById(rentalId)) {
            log.info("[{}] hold already recorded, treating redelivery as success", rentalId);
            return HoldResult.ok();
        }

        Optional<GearItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            return HoldResult.failure("Unknown item: " + itemId);
        }

        GearItem item = itemOpt.get();
        if (!item.isAvailable()) {
            log.info("[{}] item {} is not available -- rejecting", rentalId, itemId);
            return HoldResult.failure("Item " + itemId + " (\"" + item.getTitle() + "\") is not available");
        }

        item.setAvailable(false);
        itemRepository.save(item);
        holdRepository.save(new AvailabilityHold(rentalId, itemId, startDate, endDate, HoldStatus.HELD));
        log.info("[{}] held item {} from {} to {}", rentalId, itemId, startDate, endDate);
        return HoldResult.ok();
    }

    /**
     * The compensating action for {@link #holdAvailability}. Safe to call even if no hold
     * exists, or if it was already released -- compensations must be idempotent, since a
     * saga participant can't always be sure whether an earlier compensation attempt was
     * lost and is being retried.
     */
    @Transactional
    public void releaseAvailability(String rentalId) {
        Optional<AvailabilityHold> holdOpt = holdRepository.findById(rentalId);
        if (holdOpt.isEmpty()) {
            log.info("[{}] no hold on record -- nothing to release", rentalId);
            return;
        }

        AvailabilityHold hold = holdOpt.get();
        if (hold.getStatus() == HoldStatus.RELEASED) {
            log.info("[{}] hold already released -- skipping (idempotent)", rentalId);
            return;
        }

        itemRepository.findById(hold.getItemId()).ifPresent(item -> {
            item.setAvailable(true);
            itemRepository.save(item);
        });
        hold.setStatus(HoldStatus.RELEASED);
        holdRepository.save(hold);
        log.info("[{}] released hold on item {}", rentalId, hold.getItemId());
    }
}
