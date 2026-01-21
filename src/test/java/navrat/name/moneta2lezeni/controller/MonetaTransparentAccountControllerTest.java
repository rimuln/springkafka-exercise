package navrat.name.moneta2lezeni.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import navrat.name.moneta2lezeni.service.SynchronizeTransactionService;

@WebMvcTest(MonetaTransparentAccountController.class)
@AutoConfigureRestTestClient
class MonetaTransparentAccountControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private SynchronizeTransactionService orchestratorService;
    @Test
    void shouldTriggerSyncAndReturnOk() throws Exception {
        String accountNumber = "246594777";

        restTestClient.get().uri("/moneta/" + accountNumber)
                .exchangeSuccessfully()
                .expectBody(String.class)
                .isEqualTo("OK");

        verify(orchestratorService).syncTransactions(accountNumber);
    }
}