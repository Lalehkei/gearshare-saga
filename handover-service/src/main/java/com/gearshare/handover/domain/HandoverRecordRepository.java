package com.gearshare.handover.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HandoverRecordRepository extends JpaRepository<HandoverRecord, String> {
}
