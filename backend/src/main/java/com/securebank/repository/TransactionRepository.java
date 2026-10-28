package com.securebank.repository;

import com.securebank.entity.Transaction;
import com.securebank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /** All transactions for a user, newest first */
    List<Transaction> findByUserOrderByTimestampDesc(User user);

    /** All transactions (admin view), newest first */
    List<Transaction> findAllByOrderByTimestampDesc();

    /** Flagged (MEDIUM/HIGH risk) transactions, optionally filtered to a user */
    @Query("SELECT t FROM Transaction t WHERE t.riskBand IN ('HIGH','MEDIUM') AND t.status <> 'RESOLVED_SAFE' AND (:userId IS NULL OR t.user.id = :userId) ORDER BY t.timestamp DESC")
    List<Transaction> findFlagged(@Param("userId") Long userId);

    /** Transactions for a user within a time window (used for velocity check) */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.timestamp BETWEEN :from AND :to")
    List<Transaction> findByUserAndTimestampBetween(@Param("user") User user,
                                                    @Param("from") Instant from,
                                                    @Param("to") Instant to);

    /** All simulated transactions (for purge) */
    List<Transaction> findByIsSimulatedTrue();
}
