package navrat.name.vivicta.producer;

import navrat.name.vivicta.dto.TransactionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionProducerTest {

    @Mock
    private KafkaTemplate<String, TransactionDto> kafkaTemplate;

    @InjectMocks
    private KafkaTransactionProducer kafkaTransactionProducer;

    @Test
    void sendTransaction_shouldSendMessageToCorrectTopic() {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionNumber(123);

        kafkaTransactionProducer.sendTransaction(dto);

        verify(kafkaTemplate).send("new-transactions", dto);
    }
}