package com.securebank.repository;

import com.securebank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /** All transactions for a specific user, newest first */
    List<Transaction> findByUserIdOrderByTimestampDesc(String userId);

    /** All transactions ordered by newest first */
    List<Transaction> findAllByOrderByTimestampDesc();

    /** All transactions for a user (any order — used by fraud engine velocity check) */
    List<Transaction> findByUserId(String userId);

    /** Flagged transactions (MEDIUM or HIGH risk, or BLOCKED status) */
    @Query("SELECT t FROM Transaction t WHERE t.riskBand IN ('HIGH', 'MEDIUM') OR t.status = 'BLOCKED' ORDER BY t.timestamp DESC")
    List<Transaction> findFlaggedTransactions();

    /** Simulated-only transactions */
    List<Transaction> findBySimulatedTrue();

    /** Delete all simulated transactions */
    void deleteBySimulatedTrue();
}
