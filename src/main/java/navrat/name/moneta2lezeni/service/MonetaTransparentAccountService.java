package navrat.name.moneta2lezeni.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import navrat.name.moneta2lezeni.config.MonetaClient;
import navrat.name.moneta2lezeni.dto.AccountStatementDto;
import navrat.name.moneta2lezeni.dto.TransactionDto;

@Service
public class MonetaTransparentAccountService {

    private final RestClient restClient;


    public MonetaTransparentAccountService(@MonetaClient RestClient restClient) {
        this.restClient = restClient;
    }


    public AccountStatementDto getAccountStatement(String accountName, int transactionNumber, LocalDate transactionDate) {
        return restClient.get()
                .uri(uriBuilder -> {// Základní parametry
                    uriBuilder.path("/accountDetail")
                            .queryParam("reverse", "N")
                            .queryParam("accountNumber", accountName);

                    if (transactionNumber != 0) {
                        uriBuilder.queryParam("transactionNumber", transactionNumber);
                    }
                    if (transactionDate != null) {
                        uriBuilder.queryParam("transactionDate", transactionDate.toString());
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(AccountStatementDto.class);
    }

    public List<TransactionDto> fetchAllTransactions(String accountName, TransactionDto newestTransaction) {
        List<TransactionDto> transactionDtos = new ArrayList<>();
        String endOfTransactions;
        int transactionNumber = 0;
        LocalDate transactionDate = null;

        do {
            AccountStatementDto statement = getAccountStatement(accountName, transactionNumber, transactionDate);
            List<TransactionDto> currentTransactions = statement.getTransactions();
            if (currentTransactions != null && !currentTransactions.isEmpty()) {
                Optional<TransactionDto> duplicate = Optional.empty();
                if (newestTransaction != null) {
                    duplicate = currentTransactions.stream()
                            .filter(t -> t.getTransactionNumber().equals(newestTransaction.getTransactionNumber())
                                    && t.getTransactionSentDate().equals(newestTransaction.getTransactionSentDate()))
                            .findFirst();
                }

                if (duplicate.isPresent()) {
                    int index = currentTransactions.indexOf(duplicate.get());
                    transactionDtos.addAll(currentTransactions.subList(0, index));
                    break;
                } else {
                    transactionDtos.addAll(currentTransactions);
                    TransactionDto last = currentTransactions.getLast();
                    transactionDate = last.getTransactionDate();
                    transactionNumber = last.getTransactionNumber();
                }
            }
            endOfTransactions = statement.getEndOfTransactions();
        } while ("N".equals(endOfTransactions));
        transactionDtos.stream()
                .filter(TransactionDto::isOutcome)
                .forEach(TransactionDto::negateAmount);
        return transactionDtos;
    }
}