package navrat.name.vivicta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static navrat.name.vivicta.utils.DtoTestFactory.TEST_DATE;
import static navrat.name.vivicta.utils.DtoTestFactory.createAccountStatementDto;
import static navrat.name.vivicta.utils.DtoTestFactory.createTransactionDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import navrat.name.vivicta.dto.AccountStatementDto;
import navrat.name.vivicta.dto.TransactionDto;

@ExtendWith(MockitoExtension.class)
class MonetaTransparentAccountServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private MonetaTransparentAccountService serviceUnderTest;

    @Test
    void fetchAllTransactions_shouldFetchMultiplePagesUntilY() {
        var accountName = "246594777";

        // Strana 1
        var t1 = createTransactionDto(1, TEST_DATE, 4, new BigDecimal("100"));
        var page1 = createAccountStatementDto(List.of(t1), "N");

        // Strana 2
        var t2 = createTransactionDto(2, TEST_DATE, 5, new BigDecimal("200"));
        var page2 = createAccountStatementDto(List.of(t2), "Y");

        mockRestClientCall(page1, page2);

        List<TransactionDto> result = serviceUnderTest.fetchAllTransactions(accountName, null);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("100"), result.get(0).getAmount());
        assertEquals(new BigDecimal("-200"), result.get(1).getAmount(), "Výdej by měl být negován");
        verify(requestHeadersUriSpec, times(2)).uri(any(Function.class));
    }

    @Test
    void fetchAllTransactions_shouldStopAndSliceWhenDuplicateFound() {
        var accountName = "246594777";
        var date = LocalDate.of(2026, 1, 1);

        // Transakce, kterou už máme v DB
        var newestInDb = createTransactionDto(10, date);

        // API vrátí 3 transakce, jedna z nich (č. 10) je ta, co už máme
        var t12 = createTransactionDto(12, date);
        var t11 = createTransactionDto(11, date);
        var t10 = createTransactionDto(10, date);

        var statement = createAccountStatementDto(List.of(t12, t11, t10), "Y");

        mockRestClientCall(statement);

        List<TransactionDto> result = serviceUnderTest.fetchAllTransactions(accountName, newestInDb);

        assertEquals(2, result.size(), "Měly by zůstat jen 2 nové transakce (12 a 11)");
        assertEquals(12, result.get(0).getTransactionNumber());
        assertEquals(11, result.get(1).getTransactionNumber());
    }

    private void mockRestClientCall(AccountStatementDto... responses) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        if (responses.length > 1) {
            when(responseSpec.body(AccountStatementDto.class)).thenReturn(responses[0], java.util.Arrays.copyOfRange(responses, 1, responses.length));
        } else {
            when(responseSpec.body(AccountStatementDto.class)).thenReturn(responses[0]);
        }
    }
}
