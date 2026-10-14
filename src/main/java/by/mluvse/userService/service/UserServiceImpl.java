package by.mluvse.userService.service;

import by.mluvse.userService.dto.CardDto;
import by.mluvse.userService.dto.PaymentCardMapper;
import by.mluvse.userService.dto.UserDto;
import by.mluvse.userService.dto.UserMapper;
import by.mluvse.userService.entity.PaymentCard;
import by.mluvse.userService.entity.User;

import by.mluvse.userService.exceptions.NotFoundException;
import by.mluvse.userService.repository.UserRepository;
import by.mluvse.userService.specification.UserSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl  implements UserService{
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PaymentCardMapper cardMapper;

    public UserServiceImpl(UserRepository repository, UserMapper mapper, PaymentCardMapper cardMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.cardMapper = cardMapper;
    }

    @Override
    public UserDto create(UserDto dto) {
        if(repository.existsByEmail(dto.email())){
            throw new IllegalArgumentException("Email already exists:"+dto.email());
        }
        User user= mapper.toEntity(dto);
        return mapper.toDto(repository.save(user));
    }

    @Override
    public Optional<UserDto> getById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<UserDto> getAll(String name, String surname, Pageable pageable) {
        Specification<User> specification= UserSpecification.nameContains(name)
                .and(UserSpecification.surnameContains(surname));
        return repository.findAll(specification,pageable).map(mapper::toDto);
    }

    @Override
    public Optional<List<CardDto>> getCards(Long id) {
        List<CardDto> cards= new ArrayList<>();
        Optional<User> user= repository.findById(id);
        if(user.isPresent()) {
            for (PaymentCard card : user.get().getCards()) {
                cards.add(cardMapper.toDto(card));
            }
        } else {
            throw new NotFoundException("Not found user:"+id);
        }
        return Optional.of(cards);

    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user= mapper.updateEntity(dto,repository.findById(id)
                .orElseThrow(()-> new NotFoundException("User not found")));
        return mapper.toDto(user);
    }

    @Override
    public void changeStatus(Long id, boolean status) {
        UserDto user= repository.findById(id).map(mapper::toDto)
                .orElseThrow(()-> new NotFoundException("Card not found"));
        if(user.active()==status) throw new IllegalArgumentException("Status already:"+ status);
        UserDto newUser= user.withActive(status);
        repository.save(mapper.toEntity(newUser));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("User not found")));
    }
}
