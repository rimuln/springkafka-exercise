package navrat.name.moneta2lezeni.controller;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import navrat.name.moneta2lezeni.service.SynchronizeTransactionService;

@RestController
@RequestMapping("/moneta")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MonetaTransparentAccountController {

    private final SynchronizeTransactionService transactionOrchestratorService;

    @GetMapping("/{accountName}")
    @RateLimiter(name = "monetaSync", fallbackMethod = "syncRateLimitFallback")
    public ResponseEntity<Void> getAllTransactions(@PathVariable String accountName) {
       transactionOrchestratorService.syncTransactions(accountName);
       return ResponseEntity.ok().build();
    }

    ResponseEntity<Void> syncRateLimitFallback(String accountName, Throwable t) {
        return ResponseEntity.status(TOO_MANY_REQUESTS).build();
    }
}
