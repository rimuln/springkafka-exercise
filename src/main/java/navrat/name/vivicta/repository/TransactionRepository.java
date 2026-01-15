package navrat.name.vivicta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import navrat.name.vivicta.model.ProcessingStatus;
import navrat.name.vivicta.model.QTransaction;
import navrat.name.vivicta.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, QuerydslPredicateExecutor<Transaction> {
    Optional<Transaction> findByTransactionNumberAndTransactionDate(Integer transactionNumber, LocalDate transactionDate);
    Optional<Transaction> findFirstByOrderByTransactionDateDescTransactionNumberDesc();
}
