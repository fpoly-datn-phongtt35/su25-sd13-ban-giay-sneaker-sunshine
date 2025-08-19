package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.CustomerBlacklistHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerBlacklistHistoryRepository extends JpaRepository<CustomerBlacklistHistory, Long> {
    List<CustomerBlacklistHistory> findByCustomerId(Long customerId);
}

