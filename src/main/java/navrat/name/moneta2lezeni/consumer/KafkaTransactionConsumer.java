package navrat.name.moneta2lezeni.consumer;

import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.IGNORE;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.MANUALY_FIXED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.PENDING_MANUAL;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import lombok.extern.slf4j.Slf4j;
import navrat.name.moneta2lezeni.dto.TransactionDto;
import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.model.ProcessingStatus;
import navrat.name.moneta2lezeni.model.Transaction;
import navrat.name.moneta2lezeni.repository.TransactionRepository;
import navrat.name.moneta2lezeni.service.LezeniApiService;

@Slf4j
@Service
public class KafkaTransactionConsumer {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;
    private final LezeniApiService lezeniApiService;

    public KafkaTransactionConsumer(
            TransactionRepository transactionRepository,
            TransactionMapper mapper,
            LezeniApiService lezeniApiService) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.lezeniApiService = lezeniApiService;
    }

    @KafkaListener(topics = "new-transactions", groupId = "transaction-group")
    public void consume(TransactionDto dto) {
        Transaction entity = transactionRepository
                .findByTransactionNumberAndTransactionSentDate(dto.getTransactionNumber(), dto.getTransactionSentDate())
                .orElseGet(() -> mapper.toEntity(dto));

        log.info("Processing transaction {}. Current status: {}",
                dto.getIdentifier(), entity.getProcessingStatus());

        if (isInFinalState(entity)) {
            log.info("Transaction {} is already in final state {}. Skipping.", dto.getIdentifier(), entity.getProcessingStatus());
            return;
        }
        // Use Case 2: Manuálně opraveno v UI
        if (PENDING_MANUAL.equals(dto.getProcessingStatus())) {
            log.info("Manual fix detected for {}. Forcing API call.", dto.getIdentifier());
            processWithApi(entity, dto, MANUALY_FIXED);
        } else if (isEligibleForAutoProcessing(dto)) {
            processWithApi(entity, dto, AUTO_PROCESSED);
        } else {
            entity.setProcessingStatus(PENDING_MANUAL);
        }
        transactionRepository.save(entity);
        log.info("Transaction {} saved with final status {}",
                entity.getIdentifier(), entity.getProcessingStatus());
    }

    private boolean isInFinalState(Transaction entity) {
        return entity.getId() != null &&
                (entity.getProcessingStatus() == IGNORE ||
                        entity.getProcessingStatus() == AUTO_PROCESSED ||
                        entity.getProcessingStatus() == MANUALY_FIXED);
    }

    private boolean isEligibleForAutoProcessing(TransactionDto dto) {
        return dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.valueOf(50)) > 0
                && dto.getVariableSymbol() != null && dto.getVariableSymbol() > 10000 && dto.getVariableSymbol() < 99999;
    }

    private void processWithApi(Transaction entity, TransactionDto dto, ProcessingStatus successStatus) {
        ProcessingStatus resultStatus = lezeniApiService.callExternalApi(
                dto.getVariableSymbol(),
                dto.getAmount(),
                successStatus
        );
        entity.setProcessingStatus(resultStatus);
    }
}