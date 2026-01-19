package navrat.name.vivicta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.model.Transaction;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    Transaction toEntity(TransactionDto dto);
    TransactionDto toDto(Transaction entity);
    List<TransactionDto> toDtoList(List<Transaction> entities);
    List<Transaction> toEntityList(List<TransactionDto> dtos);
}
