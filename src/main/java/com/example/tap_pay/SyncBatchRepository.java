package com.example.tap_pay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SyncBatchRepository
        extends JpaRepository<SyncBatch, UUID> {

    List<SyncBatch> findByDeviceId(UUID deviceId);
}
