package navrat.name.vivicta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import navrat.name.vivicta.model.ProcessingStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"transactionNumber", "transactionDate"})
public class TransactionDto {
    private UUID id;
    private String accountName;
    private BigDecimal amount;
    private String counterpartyAccountName;
    private Integer creditDebitFlag;
    private Integer currencyCode;
    private String descr1;
    private String messageForRecipient;
    private LocalDate transactionDate;
    private Integer transactionNumber;
    private LocalDate transactionSentDate;
    private Long variableSymbol;
    private ProcessingStatus processingStatus;

    public boolean isOutcome() {
        return creditDebitFlag != null && creditDebitFlag > 4;
    }

    public void negateAmount() {
        amount = amount.negate();
    }
}