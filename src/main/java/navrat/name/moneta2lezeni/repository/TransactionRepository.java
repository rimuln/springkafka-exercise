package navrat.name.moneta2lezeni.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import navrat.name.moneta2lezeni.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, QuerydslPredicateExecutor<Transaction> {
    Optional<Transaction> findByTransactionNumberAndTransactionSentDate(Integer transactionNumber, LocalDate transactionDate);

    // Manual transactions can have null transactionDate / transactionNumber; those rows must
    // not pollute the Moneta-sync cursor (filter in MonetaTransparentAccountService relies on
    // all three fields being non-null).
    Optional<Transaction> findFirstByTransactionDateIsNotNullAndTransactionNumberIsNotNullOrderByTransactionDateDescTransactionNumberDesc();
}
