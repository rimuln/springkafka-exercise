package navrat.name.vivicta.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import navrat.name.vivicta.mapper.OrderMapper;
import navrat.name.vivicta.dto.AccountStatementDto;
import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.service.MonetaTransparentAccountService;
import navrat.name.vivicta.service.OrderIdGenerator;
import navrat.name.vivicta.service.TransactionOrchestratorService;

@RestController
@RequestMapping("/moneta")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MonetaTransparentAccountController {

    private final TransactionOrchestratorService monetaTransparentAccountService;

    @GetMapping("/{accountName}")
    public String getAllTransactions(@PathVariable String accountName) {
       monetaTransparentAccountService.syncTransactions(accountName);
       return "OK";
    }
}
