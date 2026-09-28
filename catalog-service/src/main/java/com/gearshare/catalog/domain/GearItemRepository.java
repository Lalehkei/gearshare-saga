package com.gearshare.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GearItemRepository extends JpaRepository<GearItem, String> {
}
