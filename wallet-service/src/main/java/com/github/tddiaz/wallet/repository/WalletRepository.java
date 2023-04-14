package com.github.tddiaz.wallet.repository;

import com.github.tddiaz.wallet.model.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithExclusiveLock(@Param("id") Long walletId);

    Optional<Wallet> findByIdAndCustomerId(Long walletId, Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id AND w.customerId = :customerId")
    Optional<Wallet> findByIdAndCustomerIdWithExclusiveLock(@Param("id") Long walletId, @Param("customerId") Long customerId);
}
