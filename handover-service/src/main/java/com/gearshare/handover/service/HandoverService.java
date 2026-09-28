package com.gearshare.handover.service;

import com.gearshare.handover.domain.HandoverRecord;
import com.gearshare.handover.domain.HandoverRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HandoverService {

    private final HandoverRecordRepository repository;

    @Transactional
    public HandoverRecord confirmPickup(String rentalId) {
        HandoverRecord record = repository.findById(rentalId)
                .orElseGet(() -> new HandoverRecord(rentalId, null, null, false, null, null));
        if (record.getPickedUpAt() == null) {
            record.setPickedUpAt(Instant.now());
        }
        return repository.save(record);
    }

    @Transactional
    public HandoverRecord confirmReturn(String rentalId, boolean damaged, BigDecimal deductionAmount, String description) {
        HandoverRecord record = repository.findById(rentalId)
                .orElseGet(() -> new HandoverRecord(rentalId, null, null, false, null, null));
        record.setReturnedAt(Instant.now());
        record.setDamaged(damaged);
        record.setDeductionAmount(damaged ? deductionAmount : null);
        record.setDescription(damaged ? description : null);
        return repository.save(record);
    }

    public java.util.Optional<HandoverRecord> find(String rentalId) {
        return repository.findById(rentalId);
    }
}
