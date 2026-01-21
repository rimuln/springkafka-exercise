package navrat.name.moneta2lezeni.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementDto {
    private BigDecimal balance;
    private Integer currencyCode;
    private String endOfTransactions;
    private Boolean isVisible;
    private String shortName;
    private List<TransactionDto> transactions;
    private LocalDate verificationDate;
}