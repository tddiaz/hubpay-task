package com.github.tddiaz.wallet.repository;

import com.github.tddiaz.wallet.model.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceId(String referenceId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT t FROM Transaction t WHERE t.referenceId = :referenceId")
    Optional<Transaction> findByReferenceIdWithExclusiveLock(@Param("referenceId") String referenceId);

    Page<Transaction> findAllByWalletId(Long walletId, Pageable pageable);
}
