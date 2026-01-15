package navrat.name.vivicta.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navrat.name.vivicta.dto.TransactionDto;

@Service
@RequiredArgsConstructor
public class KafkaTransactionProducer {

    private final KafkaTemplate<String, TransactionDto> kafkaTemplate;
    private static final String TOPIC = "new-transactions";

    public void sendTransaction(TransactionDto dto) {
        kafkaTemplate.send(TOPIC, dto);
    }
}