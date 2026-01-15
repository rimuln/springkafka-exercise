package navrat.name.vivicta.model;

import static jakarta.persistence.EnumType.STRING;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chs_transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    @Enumerated(STRING)
    private ProcessingStatus processingStatus;
    @Version
    private int version;
}
