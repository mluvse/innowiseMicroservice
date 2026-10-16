package by.mluvse.UserService.service;

import by.mluvse.UserService.dto.CardDto;
import by.mluvse.UserService.dto.PaymentCardMapper;
import by.mluvse.UserService.entity.PaymentCard;
import by.mluvse.UserService.entity.User;
import by.mluvse.UserService.exceptions.CardLimitException;
import by.mluvse.UserService.exceptions.DuplicateResourceException;
import by.mluvse.UserService.exceptions.NotFoundException;
import by.mluvse.UserService.repository.PaymentCardRepository;
import by.mluvse.UserService.repository.UserRepository;
import by.mluvse.UserService.specification.PaymentCardSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService{

    private final PaymentCardRepository  repository;
    private final UserRepository userRepository;
    private final PaymentCardMapper mapper;




    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#dto.userId()")
    public CardDto create(CardDto dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new NotFoundException("User not found: " + dto.userId()));
        if (repository.existsByNumber(dto.number())) {
            throw new DuplicateResourceException("Card number already exists: " + dto.number());
        }
        if (repository.countByUserId(dto.userId()) >= 5) {
            throw new CardLimitException("User already has 5 cards");
        }
        PaymentCard card = mapper.toEntity(dto);
        card.setActive(true);
        user.addCard(card);
        return mapper.toDto(repository.save(card));
    }

    @Override
    @Transactional
    @Cacheable(value = "payment_cards", key = "#id")
    public Optional<CardDto> getById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public Page<CardDto> getAll(String name, String surname, Pageable pageable) {
        Specification<PaymentCard> specification= PaymentCardSpecification.containsName(name)
                .and(PaymentCardSpecification.containsSurname(surname));

        return repository.findAll(specification,pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    @CachePut(value = "payment_cards", key = "#id")
    @CacheEvict(value = "users", key = "#dto.userId()")
    public CardDto update(Long id, CardDto dto) {
        PaymentCard card=  repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Card not found:"+ id));
        if (!card.getNumber().equals(dto.number())
                && repository.existsByNumber(dto.number())) {
            throw new DuplicateResourceException("Card number already exists: " + dto.number());
        }
        mapper.updateEntity(dto, card);
        return mapper.toDto(card);
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = "payment_cards", key = "#id"),
            evict = @CacheEvict(value = "users", allEntries = true))
    public CardDto changeStatus(Long id, boolean status) {
        PaymentCard card= repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Card not found"));
        if(card.isActive()== status) throw new IllegalArgumentException("Status already:"+ status);
        card.setActive(status);
        return mapper.toDto(card);
    }

    @Override
    @Transactional
    @Caching(
            evict ={ @CacheEvict(value = "payment_cards", key = "#id"),
                    @CacheEvict(value = "users", allEntries = true)}
    )
    public void delete(Long id) {
        PaymentCard card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));

        card.getUser().removeCard(card);
    }
}
