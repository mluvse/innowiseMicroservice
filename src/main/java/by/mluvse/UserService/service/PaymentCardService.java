package by.mluvse.UserService.service;

import by.mluvse.UserService.dto.CardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PaymentCardService {

     CardDto create(CardDto dto);

     Optional<CardDto> getById(Long id);

     Page<CardDto> getAll(String name, String surname, Pageable pageable);

     CardDto update(Long id, CardDto dto);

     CardDto changeStatus(Long id,boolean status);

     void delete(Long id);

}
