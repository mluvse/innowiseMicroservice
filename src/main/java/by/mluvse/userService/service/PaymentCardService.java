package by.mluvse.userService.service;

import by.mluvse.userService.dto.CardDto;
import by.mluvse.userService.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PaymentCardService {

     CardDto create(CardDto dto);

     Optional<CardDto> getById(Long id);

     Optional<Page<UserDto>> getAll(String name, String surname, Pageable pageable);

     CardDto update(Long id, CardDto dto);

     void changeStatus(Long id,boolean status);

     void delete(Long id);

}
