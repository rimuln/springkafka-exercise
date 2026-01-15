package navrat.name.vivicta.consumer;

import static navrat.name.vivicta.model.ProcessingStatus.AUTO_FAILED;
import static navrat.name.vivicta.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.vivicta.model.ProcessingStatus.PENDING_MANUAL;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import lombok.extern.slf4j.Slf4j;
import navrat.name.vivicta.config.LezeniClient;
import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.mapper.TransactionMapper;
import navrat.name.vivicta.model.ProcessingStatus;
import navrat.name.vivicta.repository.TransactionRepository;

@Slf4j
@Service
public class KafkaTransactionConsumer {

    private static final String BASE_URL = "https://lezeni.navrat.name/api/rest/prezence/platba/";
    private final RestClient restClient;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;

    public KafkaTransactionConsumer(
            @LezeniClient RestClient restClient,
            TransactionRepository transactionRepository,
            TransactionMapper mapper) {
        this.restClient = restClient;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "new-transactions", groupId = "transaction-group")
    public void consume(TransactionDto dto) {
        if (isEligibleForAutoProcessing(dto)) {
            String url = BASE_URL + dto.getVariableSymbol() + "/" + dto.getAmount();

            log.info("Processing transaction from Kafka. Calling: {}", url);
            try {
                var response = restClient.get()
                        .uri(url)
                        .retrieve()
                        .toEntity(String.class); // Předpokládáme, že odpověď je text/JSON

                if (response.getStatusCode().is2xxSuccessful()) {
                    updateTransactionStatus(dto, AUTO_PROCESSED);
                    log.info("External API call successful. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                } else {
                    updateTransactionStatus(dto, AUTO_FAILED);
                    log.warn("External API returned error status: {}. Body: {}", response.getStatusCode(), response.getBody());
                }
            } catch (Exception e) {
                updateTransactionStatus(dto, AUTO_FAILED);
                log.error("Failed to call external API for transaction {}: {}", dto.getTransactionNumber(), e.getMessage());
            }
        } else {
            updateTransactionStatus(dto, PENDING_MANUAL);
            log.info("Transaction {} is not eligible for processing.", dto.getTransactionNumber());
        }
    }

    private boolean isEligibleForAutoProcessing(TransactionDto dto) {
        return dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.valueOf(50)) > 0
                && dto.getVariableSymbol() != null && dto.getVariableSymbol() > 10000;
    }

    private void updateTransactionStatus(TransactionDto dto, ProcessingStatus status) {
        transactionRepository.findByTransactionNumberAndTransactionDate(
                        dto.getTransactionNumber(), dto.getTransactionDate())
                .ifPresent(entity -> {
                    entity.setProcessingStatus(status);
                    transactionRepository.save(entity);
                    log.info("Transaction {} updated to status {}", dto.getTransactionNumber(), status);
                });
    }
}