package by.mluvse.userService.service;

import by.mluvse.userService.dto.CardDto;
import by.mluvse.userService.dto.UserDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl  implements UserService{

    @Override
    public UserDto create(UserDto dto) {
        return null;
    }

    @Override
    public Optional<UserDto> getById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Page<UserDto>> getAll(String name, String surname, Pageable pageable) {
        return Optional.empty();
    }

    @Override
    public Optional<List<CardDto>> getCards(Long id) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
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
