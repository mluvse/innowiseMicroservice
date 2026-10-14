package by.mluvse.userService.dto;

import by.mluvse.userService.entity.PaymentCard;
import by.mluvse.userService.entity.User;
import org.mapstruct.*;

@Mapper(componentModel ="spring", unmappedTargetPolicy = ReportingPolicy.WARN, uses = PaymentCardMapper.class)
public interface UserMapper {


    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto dto);

    void updateEntity(UserDto dto, @MappingTarget User user);

    @AfterMapping
    default void linkCards(@MappingTarget User user){
        for( PaymentCard card:user.getCards()){
            card.setUser(user);
        }
    }
}
