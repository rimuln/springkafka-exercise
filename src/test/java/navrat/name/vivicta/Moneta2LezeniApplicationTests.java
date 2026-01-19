package navrat.name.vivicta;

import navrat.name.vivicta.controller.TransactionController;
import navrat.name.vivicta.service.MonetaTransparentAccountService;
import navrat.name.vivicta.service.SynchronizeTransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class Moneta2LezeniApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private TransactionController transactionController;

    @Autowired(required = false)
    private SynchronizeTransactionService syncService;

    @Autowired(required = false)
    private MonetaTransparentAccountService monetaService;

    @Test
    @DisplayName("Prověření startu celého Spring kontextu")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Ověření, že klíčové Beany byly úspěšně zaregistrovány")
    void importantBeansShouldBePresent() {
        assertThat(transactionController).as("TransactionController by měl být v kontextu").isNotNull();
        assertThat(syncService).as("SynchronizeTransactionService by měl být v kontextu").isNotNull();
        assertThat(monetaService).as("MonetaService by měl být v kontextu").isNotNull();
    }

    @Test
    @DisplayName("Kontrola názvu aplikace z konfigurace")
    void applicationPropertiesShouldBeLoaded() {
        String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        assertThat(appName).isNotNull().contains("moneta2Lezeni");
    }
}