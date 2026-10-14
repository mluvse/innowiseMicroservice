package by.mluvse.userService.service;

import by.mluvse.userService.dto.CardDto;
import by.mluvse.userService.dto.PaymentCardMapper;
import by.mluvse.userService.entity.PaymentCard;
import by.mluvse.userService.exceptions.CardLimitException;
import by.mluvse.userService.exceptions.NotFoundException;
import by.mluvse.userService.repository.PaymentCardRepository;
import by.mluvse.userService.repository.UserRepository;
import by.mluvse.userService.specification.PaymentCardSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentCardServiceImpl implements PaymentCardService{

    private final PaymentCardRepository  repository;
    private final UserRepository userRepository;
    private final PaymentCardMapper mapper;

    public PaymentCardServiceImpl(PaymentCardRepository repository, UserRepository userRepository, PaymentCardMapper mapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public CardDto create(CardDto dto) {
        if(repository.existsByNumber(dto.number())){
            throw new IllegalArgumentException("Card number already exists:"+dto.number());
        } else if (repository.countByUserId(dto.userId())==5||
                userRepository.findById(dto.userId()).get().getCards().size()==5) {
            throw new CardLimitException("User already has 5 cards");
        }
        PaymentCard card= mapper.toEntity(dto);
        return mapper.toDto(repository.save(card));
    }

    @Override
    public Optional<CardDto> getById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<CardDto> getAll(String name, String surname, Pageable pageable) {
        Specification<PaymentCard> specification= PaymentCardSpecification.containsName(name)
                .and(PaymentCardSpecification.containsSurname(surname));

        return repository.findAll(specification,pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    public CardDto update(Long id, CardDto dto) {
        PaymentCard card= mapper.updateEntity(dto, repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Card not found:"+ id)));
        return mapper.toDto(card);
    }

    @Override
    public void changeStatus(Long id, boolean status) {
        CardDto card= repository.findById(id).map(mapper::toDto)
                .orElseThrow(()-> new NotFoundException("Card not found"));
        if(card.active()== status) throw new IllegalArgumentException("Status already:"+ status);
        CardDto newCard= card.withActive(status);
        repository.save(mapper.toEntity(newCard));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Card not found")));
    }
}
