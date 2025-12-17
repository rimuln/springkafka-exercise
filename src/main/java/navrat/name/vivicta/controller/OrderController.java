package navrat.name.vivicta.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navrat.name.vivicta.dto.OrderDto;
import navrat.name.vivicta.mapper.OrderMapper;
import navrat.name.vivicta.model.Order;
import navrat.name.vivicta.repository.OrderRepository;
import navrat.name.vivicta.service.OrderIdGenerator;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderIdGenerator orderIdGenerator;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping
    public List<OrderDto> getAllOrders() {
        return orderMapper.toDtoList(orderRepository.findAll());
    }

    @PostMapping
    public Order createOrder(@RequestBody @Valid OrderDto orderData) {
        Order order = orderMapper.toEntity(orderData);
        order.setStatus("NEW");
        order.setOrderId(orderIdGenerator.generateOrderId());
        Order saved = orderRepository.save(order);
        kafkaTemplate.send("orders",saved.getId().toString());
        return saved;
    }
}
