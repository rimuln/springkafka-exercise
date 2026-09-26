package navrat.name.moneta2lezeni.controller;

import static navrat.name.moneta2lezeni.model.ProcessingStatus.PENDING_MANUAL;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navrat.name.moneta2lezeni.dto.TransactionDto;
import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.service.TransactionService;

@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionMapper mapper;


    @GetMapping
    public List<TransactionDto> getNotProcessedTransactions() {
        log.info("React UI requested pending transactions");
        return mapper.toDtoList(transactionService.findNotProcessed());
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<Void> updateTransaction(@PathVariable UUID transactionId, @RequestBody TransactionDto updateDto) {
        log.info("Updating transaction with ID: {}", transactionId);
        transactionService.updateTransaction(transactionId, updateDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/manual")
    public ResponseEntity<Map<String, String>> sendManualTransaction(@RequestBody TransactionDto dto) {
        dto.setProcessingStatus(PENDING_MANUAL);
        dto.setCreditDebitFlag(4);
        dto.setAccountName("ČESKÝ HOROLEZECKÝ");

        if (dto.getTransactionSentDate() == null) {
            dto.setTransactionSentDate(LocalDate.now());
        }
        if (dto.getTransactionDate() == null) {
            dto.setTransactionDate(dto.getTransactionSentDate());
        }
        if (transactionService.isDuplicateManualTransaction(dto)) {
            log.warn("Rejecting duplicate manual transaction VS {} amount {} date {}",
                    dto.getVariableSymbol(), dto.getAmount(), dto.getTransactionSentDate());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",
                    "Transakce s VS " + dto.getVariableSymbol() + " a částkou " + dto.getAmount()
                            + " ze dne " + dto.getTransactionSentDate() + " už byla vložena"));
        }
        transactionService.sendManualTransaction(dto);
        return ResponseEntity.ok().build();
    }
}
