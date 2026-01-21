package navrat.name.moneta2lezeni.repository;

import navrat.name.moneta2lezeni.model.ProcessingStatus;
import navrat.name.moneta2lezeni.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

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
                .findByTransactionNumberAndTransactionDate(123, date);

        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("Test Account");
    }

    @Test
    void findFirstByOrderByTransactionDateDescTransactionNumberDesc_shouldReturnNewest() {
        createAndPersist("Starší", LocalDate.of(2026, 1, 10), 10);
        createAndPersist("Novější stejný den", LocalDate.of(2026, 1, 17), 5);
        createAndPersist("Nejnovější", LocalDate.of(2026, 1, 17), 99);

        Optional<Transaction> result = repositoryUnderTest.findFirstByOrderByTransactionDateDescTransactionNumberDesc();

        assertThat(result).isPresent();
        assertThat(result.get().getAccountName()).isEqualTo("Nejnovější");
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