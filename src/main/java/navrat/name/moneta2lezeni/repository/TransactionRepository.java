package navrat.name.moneta2lezeni.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import navrat.name.moneta2lezeni.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, QuerydslPredicateExecutor<Transaction> {
    Optional<Transaction> findByTransactionNumberAndTransactionSentDate(Integer transactionNumber, LocalDate transactionDate);
    Optional<Transaction> findFirstByOrderByTransactionDateDescTransactionNumberDesc();
}
