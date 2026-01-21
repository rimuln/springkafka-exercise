package navrat.name.moneta2lezeni.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import navrat.name.moneta2lezeni.dto.TransactionDto;
import navrat.name.moneta2lezeni.model.Transaction;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    Transaction toEntity(TransactionDto dto);
    TransactionDto toDto(Transaction entity);
    List<TransactionDto> toDtoList(List<Transaction> entities);
    List<Transaction> toEntityList(List<TransactionDto> dtos);
}
