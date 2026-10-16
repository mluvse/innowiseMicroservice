package by.mluvse.UserService.dto;

import by.mluvse.UserService.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PaymentCardMapper {
    @Mapping(source="user.id", target = "userId")
    @Mapping(target = "active", source = "active")
    CardDto toDto(PaymentCard card);

    @Mapping(target="id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target="updatedAt", ignore = true)
    PaymentCard toEntity(CardDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target= "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    PaymentCard updateEntity(CardDto dto, @MappingTarget PaymentCard card);
}
