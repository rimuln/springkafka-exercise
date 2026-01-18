package navrat.name.vivicta.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

class TransactionDtoTest {

    @CsvSource({
            "1, false",
            "2, false",
            "3, false",
            "4, false",
            "5, true",
            "6, true",
            "7, true"
    })
    @ParameterizedTest
    void shouldIdentifyOutcomeWhenFlagIsGreaterThanFour(int creditDebitFlagValue, boolean expectedOutcome) {
        var transactionDto = new TransactionDto();
        transactionDto.setCreditDebitFlag(creditDebitFlagValue);
        assertThat(transactionDto.isOutcome()).isEqualTo(expectedOutcome);
    }

    @Test
    void shouldNegatePositiveAmount() {
        TransactionDto transaction = new TransactionDto();
        transaction.setAmount(BigDecimal.valueOf(100));
        assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(100));
        transaction.negateAmount();
        assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(-100));
    }
}