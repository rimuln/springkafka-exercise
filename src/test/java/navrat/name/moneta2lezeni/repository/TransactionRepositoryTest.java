package navrat.name.moneta2lezeni.repository;

import navrat.name.moneta2lezeni.model.ProcessingStatus;
import navrat.name.moneta2lezeni.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.*;
import static navrat.name.moneta2lezeni.model.QTransaction.transaction;
import static navrat.name.moneta2lezeni.utils.EntityTestFactory.createTransactionEntity;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repositoryUnderTest;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByTransactionNumberAndTransactionDate_shouldReturnTransaction() {
        LocalDate date = LocalDate.of(2026, 1, 17);
        Transaction t = createTransactionEntity("Test Account", date, 123, PENDING);
        t.setId(null);
        entityManager.persistAndFlush(t);

        Optional<Transaction> found = repositoryUnderTest
                .findByTransactionNumberAndTransactionSentDate(123, date);

        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("Test Account");
    }

    @Test
    void findNewest_shouldReturnNewestAndIgnoreNullDateRows() {
        // Manual transactions can have null transactionDate / transactionNumber.
        // PostgreSQL puts NULLs first in ORDER BY DESC, so without the IsNotNull guard
        // these rows would shadow real Moneta transactions and break the sync cursor.
        createAndPersist("Starší", LocalDate.of(2026, 1, 10), 10);
        createAndPersist("Novější stejný den", LocalDate.of(2026, 1, 17), 5);
        createAndPersist("Nejnovější", LocalDate.of(2026, 1, 17), 99);
        createAndPersist("Manualni s NULL date", null, null);
        createAndPersist("Manualni s NULL number", LocalDate.of(2026, 1, 18), null);

        Optional<Transaction> result = repositoryUnderTest
                .findFirstByTransactionSentDateIsNotNullAndTransactionNumberIsNotNullOrderByTransactionSentDateDescTransactionNumberDesc();

        assertThat(result).isPresent();
        assertThat(result.get().getAccountName()).isEqualTo("Nejnovější");
    }

    @Test
    void findNewest_shouldOrderBySentDateNotTransactionDate() {
        // Weekend payment booked on 29.9. is newer on the Moneta statement than a payment
        // made and booked on 28.9., although its transactionDate (27.9.) is older.
        var booked28 = createTransactionEntity("Zaúčtováno 28.9.", LocalDate.of(2026, 9, 28), 12, PENDING);
        booked28.setId(null);
        entityManager.persist(booked28);
        var weekend = createTransactionEntity("Víkend, zaúčtováno 29.9.", LocalDate.of(2026, 9, 27), 3, PENDING);
        weekend.setTransactionSentDate(LocalDate.of(2026, 9, 29));
        weekend.setId(null);
        entityManager.persist(weekend);

        Optional<Transaction> result = repositoryUnderTest
                .findFirstByTransactionSentDateIsNotNullAndTransactionNumberIsNotNullOrderByTransactionSentDateDescTransactionNumberDesc();

        assertThat(result).isPresent();
        assertThat(result.get().getAccountName()).isEqualTo("Víkend, zaúčtováno 29.9.");
    }

    @Test
    void existsManualDuplicate_shouldMatchOnVsAmountDateAndStatus() {
        LocalDate date = LocalDate.of(2026, 9, 25);
        Transaction t = createTransactionEntity(null, date, MANUALY_FIXED);
        t.setId(null);
        t.setVariableSymbol(36149L);
        t.setAmount(new BigDecimal("1000.00"));
        entityManager.persistAndFlush(t);

        assertThat(repositoryUnderTest.existsByVariableSymbolAndAmountAndTransactionSentDateAndProcessingStatus(
                36149L, BigDecimal.valueOf(1000), date, MANUALY_FIXED)).isTrue();
        assertThat(repositoryUnderTest.existsByVariableSymbolAndAmountAndTransactionSentDateAndProcessingStatus(
                36157L, BigDecimal.valueOf(1000), date, MANUALY_FIXED)).isFalse();
        assertThat(repositoryUnderTest.existsByVariableSymbolAndAmountAndTransactionSentDateAndProcessingStatus(
                36149L, BigDecimal.valueOf(500), date, MANUALY_FIXED)).isFalse();
        assertThat(repositoryUnderTest.existsByVariableSymbolAndAmountAndTransactionSentDateAndProcessingStatus(
                36149L, BigDecimal.valueOf(1000), date.minusDays(1), MANUALY_FIXED)).isFalse();
    }

    @Test
    void queryDslFilter_shouldWorkWithNotIn() {
        var expectedItem = createAndPersistWithStatus(PENDING_MANUAL);
        createAndPersistWithStatus(IGNORE);
        createAndPersistWithStatus(AUTO_PROCESSED);
        createAndPersistWithStatus(MANUALY_FIXED);

        var predicate = transaction.processingStatus.notIn(
                AUTO_PROCESSED,
                IGNORE,
                MANUALY_FIXED
        );

        var result = repositoryUnderTest.findAll(predicate);

        assertThat(result).containsExactly(expectedItem);
    }

    private void createAndPersist(String name, LocalDate date, Integer number) {
        var t = createTransactionEntity(name, date, number, PENDING);
        t.setId(null);
        entityManager.persist(t);
    }

    private Transaction createAndPersistWithStatus(ProcessingStatus status) {
        var t = createTransactionEntity(status);
        t.setTransactionNumber(status.ordinal() + 100);
        t.setId(null);
        return entityManager.persist(t);
    }
}