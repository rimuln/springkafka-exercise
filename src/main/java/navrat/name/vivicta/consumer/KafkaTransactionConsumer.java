package navrat.name.vivicta.consumer;

import static navrat.name.vivicta.model.ProcessingStatus.AUTO_FAILED;
import static navrat.name.vivicta.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.vivicta.model.ProcessingStatus.MANUALY_FIXED;
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
import navrat.name.vivicta.model.Transaction;
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
        Transaction entity = transactionRepository
                .findByTransactionNumberAndTransactionDate(dto.getTransactionNumber(), dto.getTransactionDate())
                .orElseGet(() -> mapper.toEntity(dto));

        log.info("Processing transaction {}. Current status: {}",
                dto.getTransactionNumber(), entity.getProcessingStatus());

        // Use Case 2: Manuálně opraveno v UI
        if (PENDING_MANUAL.equals(dto.getProcessingStatus())) {
            log.info("Manual fix detected for {}. Forcing API call.", dto.getTransactionNumber());
            executeApiCallAndSetStatus(entity, dto,MANUALY_FIXED);
        }
        // Use Case 1: Nová transakce k analýze
        else if (isEligibleForAutoProcessing(dto)) {
            executeApiCallAndSetStatus(entity, dto, AUTO_PROCESSED);
        }
        else {
            entity.setProcessingStatus(PENDING_MANUAL);
        }

        transactionRepository.save(entity);
        log.info("Transaction {} saved with final status {}",
                entity.getTransactionNumber(), entity.getProcessingStatus());
    }

    private boolean isEligibleForAutoProcessing(TransactionDto dto) {
        return dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.valueOf(50)) > 0
                && dto.getVariableSymbol() != null && dto.getVariableSymbol() > 10000;
    }

    private void executeApiCallAndSetStatus(Transaction entity, TransactionDto dto, ProcessingStatus processingStatus) {
        String url = BASE_URL + dto.getVariableSymbol() + "/" + dto.getAmount();
        try {
            var response = restClient.get().uri(url).retrieve().toEntity(String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                entity.setProcessingStatus(processingStatus);
            } else {
                entity.setProcessingStatus(AUTO_FAILED);
            }
        } catch (Exception e) {
            log.error("API call failed for {}: {}", dto.getTransactionNumber(), e.getMessage());
            entity.setProcessingStatus(AUTO_FAILED);
        }
    }
}