package by.mluvse.UserService.service;

import by.mluvse.UserService.dto.CardDto;
import by.mluvse.UserService.entity.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PaymentCardService {

     CardDto create(CardDto dto, AuthUser authUser);

     Optional<CardDto> getById(Long id, AuthUser user);

     Page<CardDto> getAll(String name, String surname, Pageable pageable);

     CardDto update(Long id, CardDto dto, AuthUser user);

     CardDto changeStatus(Long id,boolean status, AuthUser user);

     void delete(Long id, AuthUser user);

}
