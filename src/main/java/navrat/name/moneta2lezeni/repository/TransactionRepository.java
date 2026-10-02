package navrat.name.moneta2lezeni.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import navrat.name.moneta2lezeni.model.ProcessingStatus;
import navrat.name.moneta2lezeni.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, QuerydslPredicateExecutor<Transaction> {
    Optional<Transaction> findByTransactionNumberAndTransactionSentDate(Integer transactionNumber, LocalDate transactionDate);

    // Moneta orders (and numbers) transactions by transactionSentDate, not transactionDate —
    // a weekend payment has an older transactionDate but is the newest on the statement.
    // Manual transactions can have null dates / transactionNumber; those rows must
    // not pollute the Moneta-sync cursor (filter in MonetaTransparentAccountService relies on
    // all three fields being non-null).
    Optional<Transaction> findFirstByTransactionSentDateIsNotNullAndTransactionNumberIsNotNullOrderByTransactionSentDateDescTransactionNumberDesc();

    boolean existsByVariableSymbolAndAmountAndTransactionSentDateAndProcessingStatus(
            Long variableSymbol, BigDecimal amount, LocalDate transactionSentDate, ProcessingStatus processingStatus);
}
