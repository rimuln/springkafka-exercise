package navrat.name.vivicta.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
        private UUID id;
        private String orderId;
        private String customerName;
        private String productName;
}
