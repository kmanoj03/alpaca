package com.rvy.scanner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rvy.scanner.entity.SavedScan;

public interface SavedScanRepository extends JpaRepository<SavedScan, Long> {

    @Query("select distinct s from SavedScan s left join fetch s.candidates order by s.scannedAt desc")
    List<SavedScan> findAllByOrderByScannedAtDesc();
}
