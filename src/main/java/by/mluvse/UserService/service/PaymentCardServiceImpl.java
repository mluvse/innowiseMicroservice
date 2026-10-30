package by.mluvse.UserService.service;

import by.mluvse.UserService.dto.CardDto;
import by.mluvse.UserService.dto.PaymentCardMapper;
import by.mluvse.UserService.entity.AuthUser;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService{

    private static final int MAX_ACTIVE_CARDS = 5;

    private final PaymentCardRepository  repository;
    private final UserRepository userRepository;
    private final PaymentCardMapper mapper;




    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#dto.userId()")
    public CardDto create(CardDto dto, AuthUser authUser) {
        if (!authUser.isAdmin() && !dto.userId().equals(authUser.userId())) {
            throw new AccessDeniedException("You can create cards only for yourself");
        }
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new NotFoundException("User not found: " + dto.userId()));
        if (repository.existsByNumber(dto.number())) {
            throw new DuplicateResourceException("Card number already exists: " + dto.number());
        }
        if (repository.countByUserId(dto.userId()) >= 5) {
            throw new CardLimitException("User already has 5 cards");
        }
        PaymentCard card = mapper.toEntity(dto);
        checkLimit(dto.userId());
        card.setActive(true);
        user.addCard(card);
        return mapper.toDto(repository.save(card));
    }

    @Override
    @Transactional
    public Optional<CardDto> getById(Long id, AuthUser user) {
        PaymentCard card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        checkOwner(card, user);
        return Optional.ofNullable(mapper.toDto(card));
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
    public CardDto update(Long id, CardDto dto, AuthUser user) {
        PaymentCard card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        checkOwner(card, user);
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
    public CardDto changeStatus(Long id, boolean status, AuthUser user) {
        PaymentCard card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        checkOwner(card, user);
        if(card.isActive()== status) throw new IllegalArgumentException("Status already:"+ status);
        checkLimit(id);
        card.setActive(status);
        return mapper.toDto(card);
    }

    @Override
    @Transactional
    @Caching(
            evict ={ @CacheEvict(value = "payment_cards", key = "#id"),
                    @CacheEvict(value = "users", allEntries = true)}
    )
    public void delete(Long id, AuthUser user) {
        PaymentCard card = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        checkOwner(card, user);
        card.getUser().removeCard(card);
    }

    private void checkOwner(PaymentCard card, AuthUser user) {
        if (user.isAdmin()) {
            return;
        }
        if (!card.getUser().getId().equals(user.userId())) {
            throw new AccessDeniedException("You can access only your own cards");
        }
    }

    private void checkLimit(Long userId) {
        if (repository.countByUserIdAndActiveTrue(userId) >= MAX_ACTIVE_CARDS) {
            throw new CardLimitException("User already has " + MAX_ACTIVE_CARDS + " active cards");
        }
    }
}
