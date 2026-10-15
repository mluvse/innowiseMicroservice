package by.mluvse.userService.service;

import by.mluvse.userService.dto.CardDto;
import by.mluvse.userService.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

     UserDto create(UserDto dto);

     UserDto getById(Long id);

     Page<UserDto> getAll(String name, String surname, Pageable pageable);

     List<CardDto> getCards(Long id);

    UserDto update(Long id, UserDto dto);

    UserDto changeStatus(Long id, boolean status);

    void delete(Long id);

}
