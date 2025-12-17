package navrat.name.vivicta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import navrat.name.vivicta.dto.OrderDto;
import navrat.name.vivicta.model.Order;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    @Mapping(target = "status", constant = "NEW")
    Order toEntity(OrderDto dto);
    OrderDto toDto(Order entity);
    List<OrderDto> toDtoList(List<Order> entities);
}
