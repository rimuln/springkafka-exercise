package navrat.name.vivicta.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import navrat.name.vivicta.dto.AccountStatementDto;
import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.model.ProcessingStatus;

public class DtoTestFactory {

    public static final LocalDate TEST_DATE = LocalDate.of(2026, 1, 14);
    public static final Integer TEST_NUMBER = 3;

    public static TransactionDto createTransactionDto() {
        return createTransactionDto(TEST_NUMBER, TEST_DATE);
    }

    public static TransactionDto createTransactionDto(ProcessingStatus status) {
        var dto = createTransactionDto();
        dto.setProcessingStatus(status);
        return dto;
    }

    public static TransactionDto createTransactionDto(UUID id, ProcessingStatus status, Long vs) {
        var dto = createTransactionDto(status);
        dto.setId(id);
        dto.setVariableSymbol(vs);
        return dto;
    }

    public static TransactionDto createTransactionDto(Integer number, LocalDate date) {
        return createTransactionDto(number, date, 4, BigDecimal.valueOf(100));
    }

    public static TransactionDto createTransactionDto(Integer number, LocalDate date, Integer flag, BigDecimal amount) {
        var dto = new TransactionDto();
        dto.setId(UUID.randomUUID());
        dto.setTransactionNumber(number);
        dto.setTransactionDate(date);
        dto.setCreditDebitFlag(flag);
        dto.setAmount(amount);
        dto.setCurrencyCode(0);
        return dto;
    }

    public static AccountStatementDto createAccountStatementDto(List<TransactionDto> transactions, String endOfTransactions) {
        var dto = new AccountStatementDto();
        dto.setTransactions(transactions);
        dto.setEndOfTransactions(endOfTransactions);
        return dto;
    }
}
