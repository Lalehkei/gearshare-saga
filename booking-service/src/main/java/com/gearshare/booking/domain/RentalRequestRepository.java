package com.gearshare.booking.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRequestRepository extends JpaRepository<RentalRequestEntity, String> {
}
