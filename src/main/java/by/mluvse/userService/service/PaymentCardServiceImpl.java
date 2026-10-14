package by.mluvse.userService.service;

import by.mluvse.userService.dto.CardDto;
import by.mluvse.userService.dto.UserDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentCardServiceImpl implements PaymentCardService{
    @Override
    public CardDto create(CardDto dto) {
        return null;
    }

    @Override
    public Optional<CardDto> getById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Page<UserDto>> getAll(String name, String surname, Pageable pageable) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public CardDto update(Long id, CardDto dto) {
        return null;
    }

    @Override
    public void changeStatus(Long id, boolean status) {

    }

    @Override
    @Transactional
    public void delete(Long id) {

    }
}
