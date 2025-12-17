package navrat.name.vivicta.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import navrat.name.vivicta.model.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
