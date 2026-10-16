package by.mluvse.UserService.service;

import by.mluvse.UserService.dto.CardDto;
import by.mluvse.UserService.dto.PaymentCardMapper;
import by.mluvse.UserService.dto.UserDto;
import by.mluvse.UserService.dto.UserMapper;
import by.mluvse.UserService.entity.User;

import by.mluvse.UserService.exceptions.CardLimitException;
import by.mluvse.UserService.exceptions.NotFoundException;
import by.mluvse.UserService.repository.UserRepository;
import by.mluvse.UserService.specification.UserSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService{
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PaymentCardMapper cardMapper;
    private static final int MAX_CARDS_PER_USER = 5;


    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        if(repository.existsByEmail(dto.email())){
            throw new IllegalArgumentException("Email already exists:"+dto.email());
        }
        if (dto.cards() != null && dto.cards().size() > MAX_CARDS_PER_USER) {
            throw new CardLimitException("User cannot have more than " + MAX_CARDS_PER_USER + " cards");
        }
        User user= mapper.toEntity(dto);
        user.setActive(true);
        return mapper.toDto(repository.save(user));
    }

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#id")
    public UserDto getById(Long id) {
        return repository.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Override
    @Transactional
    public Page<UserDto> getAll(String name, String surname, Pageable pageable) {
        Specification<User> specification= UserSpecification.nameContains(name)
                .and(UserSpecification.surnameContains(surname));
        return repository.findAll(specification,pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    public List<CardDto> getCards(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return user.getCards().stream().map(cardMapper::toDto).toList();

    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDto update(Long id, UserDto dto) {
        User user= repository.findById(id)
                .orElseThrow(()-> new NotFoundException("User not found"));
        mapper.updateEntity(dto, user);
        return mapper.toDto(user);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDto changeStatus(Long id, boolean status) {
        User user= repository.findById(id)
                .orElseThrow(()-> new NotFoundException("User not found"));
        if(user.isActive()==status) throw new IllegalArgumentException("Status already:"+ status);
        user.setActive(status);
        return mapper.toDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users")
    public void delete(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(()-> new NotFoundException("User not found")));
    }
}
