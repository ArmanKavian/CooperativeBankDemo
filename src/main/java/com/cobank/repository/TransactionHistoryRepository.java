package com.cobank.repository;

import com.cobank.domain.TransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, UUID> {
    Page<TransactionHistory> findByIbanOrderByTimestampDesc(String iban, Pageable pageable);
}