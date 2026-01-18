package navrat.name.vivicta.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import navrat.name.vivicta.model.ProcessingStatus;
import navrat.name.vivicta.model.Transaction;

public class EntityTestFactory {

    public static final LocalDate TEST_DATE = LocalDate.of(2026, 1, 14);
    public static final Integer TEST_NUMBER = 3;

    public static Transaction createTransactionEntity() {
        return createTransactionEntity(TEST_NUMBER, TEST_DATE);
    }

    public static Transaction createTransactionEntity(ProcessingStatus status) {
        var entity = createTransactionEntity();
        entity.setProcessingStatus(status);
        return entity;
    }

    public static Transaction createTransactionEntity(UUID id, ProcessingStatus status, Long vs) {
        var entity = createTransactionEntity(status);
        entity.setId(id);
        entity.setVariableSymbol(vs);
        return entity;
    }

    public static Transaction createTransactionEntity(String accountName, LocalDate date, Integer number, ProcessingStatus status) {
        var entity = createTransactionEntity(number, date);
        entity.setAccountName(accountName);
        entity.setProcessingStatus(status);
        return entity;
    }

    public static Transaction createTransactionEntity(Integer number, LocalDate date, ProcessingStatus status) {
        var entity = createTransactionEntity(number, date);
        entity.setProcessingStatus(status);
        return entity;
    }

    public static Transaction createTransactionEntity(Integer number, LocalDate date) {
        var entity = new Transaction();
        entity.setId(UUID.randomUUID());
        entity.setTransactionNumber(number);
        entity.setTransactionDate(date);
        entity.setAmount(BigDecimal.valueOf(100));
        entity.setCreditDebitFlag(4);
        entity.setCurrencyCode(0);
        return entity;
    }
}
