package by.mluvse.UserService.serviceTests;

import by.mluvse.UserService.dto.CardDto;
import by.mluvse.UserService.dto.PaymentCardMapper;
import by.mluvse.UserService.entity.PaymentCard;
import by.mluvse.UserService.entity.User;
import by.mluvse.UserService.exceptions.CardLimitException;
import by.mluvse.UserService.exceptions.DuplicateResourceException;
import by.mluvse.UserService.exceptions.NotFoundException;
import by.mluvse.UserService.repository.PaymentCardRepository;
import by.mluvse.UserService.repository.UserRepository;
import by.mluvse.UserService.service.PaymentCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    PaymentCardRepository repository;
    @Mock
    UserRepository userRepository;
    @Mock
    PaymentCardMapper mapper;
    @InjectMocks
    PaymentCardServiceImpl service;

    private User testUser;
    private PaymentCard testCard;
    private CardDto testCardDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Ivan");
        testUser.setSurname("Petrov");
        testUser.setEmail("ivan@mail.com");
        testUser.setActive(true);

        testCard = new PaymentCard();
        testCard.setId(10L);
        testCard.setNumber("4111111111111111");
        testCard.setHolder("IVAN PETROV");
        testCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        testCard.setActive(true);
        testUser.addCard(testCard);

        testCardDto = new CardDto(10L, 1L, "4111111111111111", "IVAN PETROV",
                LocalDate.of(2030, 12, 31), true);
    }

    @Test
    void create_whenValid_savesAndReturnsDto() {
        PaymentCard newCard = new PaymentCard();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toEntity(testCardDto)).thenReturn(newCard);
        when(repository.save(newCard)).thenReturn(newCard);
        when(mapper.toDto(newCard)).thenReturn(testCardDto);

        CardDto result = service.create(testCardDto);

        assertEquals(testCardDto, result);
        assertSame(testUser, newCard.getUser());
        verify(repository).save(newCard);
    }

    @Test
    void create_whenUserNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(testCardDto));
        verify(repository, never()).save(any());
    }

    @Test
    void create_whenNumberExists_throwsDuplicateResourceException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.existsByNumber(testCardDto.number())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(testCardDto));
        verify(repository, never()).save(any());
    }

    @Test
    void create_whenUserHasFiveCards_throwsCardLimitException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repository.countByUserId(1L)).thenReturn(5L);

        assertThrows(CardLimitException.class, () -> service.create(testCardDto));
        verify(repository, never()).save(any());
    }

    @Test
    void getById_whenFound_returnsDto() {
        when(repository.findById(10L)).thenReturn(Optional.of(testCard));
        when(mapper.toDto(testCard)).thenReturn(testCardDto);

        Optional<CardDto> result = service.getById(10L);

        assertTrue(result.isPresent());
        assertEquals(testCardDto, result.get());
    }

    @Test
    void getById_whenNotFound_returnsEmpty() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertTrue(service.getById(10L).isEmpty());
    }

    @Test
    void getAll_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(testCard), pageable, 1));
        when(mapper.toDto(testCard)).thenReturn(testCardDto);

        Page<CardDto> result = service.getAll("Ivan", "Petrov", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testCardDto, result.getContent().get(0));
    }

    @Test
    void update_whenValid_updatesAndReturnsDto() {
        when(repository.findById(10L)).thenReturn(Optional.of(testCard));
        when(mapper.toDto(testCard)).thenReturn(testCardDto);

        CardDto result = service.update(10L, testCardDto);

        assertEquals(testCardDto, result);
        verify(mapper).updateEntity(testCardDto, testCard);
    }

    @Test
    void update_whenCardNotFound_throwsNotFoundException() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(10L, testCardDto));
        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_whenNumberBelongsToAnotherCard_throws() {
        CardDto other = new CardDto(10L, 1L, "5555555555554444", "IVAN PETROV",
                LocalDate.of(2030, 12, 31), true);
        when(repository.findById(10L)).thenReturn(Optional.of(testCard));
        when(repository.existsByNumber("5555555555554444")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.update(10L, other));
        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void changeStatus_whenDifferent_updatesAndReturnsDto() {
        CardDto deactivated = new CardDto(10L, 1L, "4111111111111111", "IVAN PETROV",
                LocalDate.of(2030, 12, 31), false);
        when(repository.findById(10L)).thenReturn(Optional.of(testCard)); // active = true
        when(mapper.toDto(testCard)).thenReturn(deactivated);

        CardDto result = service.changeStatus(10L, false);

        assertFalse(testCard.isActive());
        assertFalse(result.active());
    }

    @Test
    void changeStatus_whenStatusAlreadySet_throwsIllegalArgumentException() {
        when(repository.findById(10L)).thenReturn(Optional.of(testCard));

        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(10L, true));
        verify(mapper, never()).toDto(any());
    }

    @Test
    void changeStatus_whenCardNotFound_throwsNotFoundException() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.changeStatus(10L, false));
    }

    @Test
    void delete_whenCardExists_removesCardFromUser() {
        when(repository.findById(10L)).thenReturn(Optional.of(testCard));

        service.delete(10L);

        assertFalse(testUser.getCards().contains(testCard));
        assertNull(testCard.getUser());
    }

    @Test
    void delete_whenCardNotFound_throwsNotFoundException() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(10L));
    }
}
