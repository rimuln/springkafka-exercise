package navrat.name.moneta2lezeni.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.service.TransactionService;

@WebMvcTest(TransactionController.class)
@AutoConfigureRestTestClient
class TransactionControllerTest {

    private static final String MANUAL_BODY =
            "{\"variableSymbol\":36149,\"amount\":1000,\"transactionSentDate\":\"2026-09-25\"}";

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private TransactionMapper mapper;

    @Test
    void sendManualTransaction_shouldPublish_whenNotDuplicate() {
        when(transactionService.isDuplicateManualTransaction(any())).thenReturn(false);

        restTestClient.post().uri("/transactions/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .body(MANUAL_BODY)
                .exchange()
                .expectStatus().isOk();

        verify(transactionService).sendManualTransaction(any());
    }

    @Test
    void sendManualTransaction_shouldReturnConflict_whenDuplicate() {
        when(transactionService.isDuplicateManualTransaction(any())).thenReturn(true);

        restTestClient.post().uri("/transactions/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .body(MANUAL_BODY)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.message").exists();

        verify(transactionService, never()).sendManualTransaction(any());
    }
}
