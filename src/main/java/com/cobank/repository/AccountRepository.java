package com.cobank.repository;

import com.cobank.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByIban(String iban);

    @Query("SELECT a FROM Account a WHERE a.iban = :iban")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByIbanForUpdate(@Param("iban") String iban);
}
