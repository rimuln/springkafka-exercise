package navrat.name.vivicta.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navrat.name.vivicta.service.SynchronizeTransactionService;

@RestController
@RequestMapping("/moneta")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MonetaTransparentAccountController {

    private final SynchronizeTransactionService transactionOrchestratorService;

    @GetMapping("/{accountName}")
    public String getAllTransactions(@PathVariable String accountName) {
       transactionOrchestratorService.syncTransactions(accountName);
       return "OK";
    }
}
