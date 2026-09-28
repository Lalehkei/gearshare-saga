package com.gearshare.booking.service;

import com.gearshare.booking.domain.RentalRequestEntity;
import com.gearshare.booking.domain.RentalRequestEntity.Status;
import com.gearshare.booking.domain.RentalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final RentalRequestRepository repository;

    @Transactional
    public RentalRequestEntity create(String itemId, String renterId, String ownerId,
                                       LocalDate startDate, LocalDate endDate, BigDecimal totalPrice) {
        String rentalId = UUID.randomUUID().toString();
        RentalRequestEntity entity = new RentalRequestEntity(
                rentalId, itemId, renterId, ownerId, startDate, endDate, totalPrice, Status.PENDING, null, Instant.now());
        repository.save(entity);
        log.info("[{}] rental request created, saga #1 starting", rentalId);
        return entity;
    }

    @Transactional
    public void markConfirmed(String rentalId) {
        repository.findById(rentalId).ifPresent(entity -> {
            if (entity.getStatus() != Status.PENDING) {
                log.info("[{}] already {}, ignoring RentalConfirmed", rentalId, entity.getStatus());
                return;
            }
            entity.setStatus(Status.CONFIRMED);
            repository.save(entity);
            log.info("[{}] CONFIRMED", rentalId);
        });
    }

    @Transactional
    public void markCancelled(String rentalId, String reason) {
        repository.findById(rentalId).ifPresent(entity -> {
            if (entity.getStatus() != Status.PENDING) {
                log.info("[{}] already {}, ignoring RentalCancelled", rentalId, entity.getStatus());
                return;
            }
            entity.setStatus(Status.CANCELLED);
            entity.setReason(reason);
            repository.save(entity);
            log.info("[{}] CANCELLED ({})", rentalId, reason);
        });
    }

    public Optional<RentalRequestEntity> find(String rentalId) {
        return repository.findById(rentalId);
    }

    public List<RentalRequestEntity> findAll() {
        return repository.findAll();
    }
}
