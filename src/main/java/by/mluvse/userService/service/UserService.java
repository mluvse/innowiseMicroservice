package by.mluvse.userService.service;

import by.mluvse.userService.dto.CardDto;
import by.mluvse.userService.dto.UserDto;
import by.mluvse.userService.entity.User;
import org.springframework.data.domain.Page;


import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserService {

     UserDto create(UserDto dto);

     Optional<UserDto> getById(Long id);

     Optional<Page<UserDto>> getAll(String name, String surname, Pageable pageable);

     Optional<List<CardDto>> getCards(Long id);

    UserDto update(Long id, UserDto dto);

    void changeStatus(Long id,boolean status);

    void delete(Long id);

}
