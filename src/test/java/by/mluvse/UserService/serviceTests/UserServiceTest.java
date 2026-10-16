package by.mluvse.UserService.serviceTests;

import by.mluvse.UserService.dto.CardDto;
import by.mluvse.UserService.dto.PaymentCardMapper;
import by.mluvse.UserService.dto.UserDto;
import by.mluvse.UserService.dto.UserMapper;
import by.mluvse.UserService.entity.PaymentCard;
import by.mluvse.UserService.entity.User;
import by.mluvse.UserService.exceptions.NotFoundException;
import by.mluvse.UserService.repository.UserRepository;
import by.mluvse.UserService.service.UserServiceImpl;
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
public class UserServiceTest {
    @Mock
    UserRepository repository;
    @Mock
    UserMapper userMapper;
    @Mock
    PaymentCardMapper cardMapper;
    @InjectMocks
    UserServiceImpl userService;

    private  User testUser;
    private UserDto testUserDto;
    private CardDto testCardDto;

    @BeforeEach
    void setUp(){

        testUser= new User();
        testUser.setId(1L);
        testUser.setName("Ivan");
        testUser.setSurname("Petrov");
        testUser.setBirthDate(LocalDate.of(1995, 5, 20));
        testUser.setEmail("ivan@mail.com");
        testUser.setActive(true);

        testCardDto= new CardDto(1L,10L,"123456789123","IVAN PETROV",LocalDate.of(2030, 12, 31),true);
        testUserDto = new UserDto(1L, "Ivan", "Petrov", LocalDate.of(1995, 5, 20),
                "ivan@mail.com", List.of(testCardDto), true);

        PaymentCard card = new PaymentCard();   // обязательно new
        card.setId(10L);
        card.setNumber("123456789123");
        card.setHolder("IVAN PETROV");
        card.setExpirationDate(LocalDate.of(2030, 12, 31));
        card.setActive(true);

        testUser.addCard(card);
    }

    @Test
    void getById_whenNotFound_throwsNotFoundException(){
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,()->userService.getById(1L));
    }

    @Test
    void getByIdTest(){
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.getById(1L);

        assertEquals(testUserDto, result);
    }

    @Test
    void getAllTest(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> usersPage = new PageImpl<>(List.of(testUser), pageable, 1);
        UserDto dto = new UserDto(1L, "Ivan", "Petrov", LocalDate.of(1995, 5, 20),
                "ivan@mail.com", List.of(), true);
        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(usersPage);
        when(userMapper.toDto(testUser)).thenReturn(dto);

        Page<UserDto> result = userService.getAll("Ivan", "Petrov", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAll_whenNothingFound_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<UserDto> result = userService.getAll(null, null, pageable);

        assertTrue(result.isEmpty());
        verifyNoInteractions(userMapper);
    }

    @Test
    void getCards(){
        CardDto cardDto = new CardDto(10L, 1L, "4111111111111111", "IVAN PETROV",
                LocalDate.of(2030, 12, 31), true);
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardMapper.toDto(any(PaymentCard.class))).thenReturn(cardDto);

        List<CardDto> result = userService.getCards(1L);

        assertEquals(1, result.size());
        assertEquals(cardDto, result.get(0));
    }


    @Test
    void getCards_whenUserNotFound_throwsNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getCards(1L));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void create_whenValid_savesAndReturnsDto() {
        when(repository.existsByEmail(testUserDto.email())).thenReturn(false);
        when(userMapper.toEntity(testUserDto)).thenReturn(testUser);
        when(repository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.create(testUserDto);

        assertEquals(testUserDto, result);
        verify(repository).save(testUser);
    }

    @Test
    void create_whenEmailExists_throwsAndDoesNotSave() {
        when(repository.existsByEmail(testUserDto.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.create(testUserDto));
        verify(repository, never()).save(any());
    }

    @Test
    void update_whenUserExists_updatesAndReturnsDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.update(1L, testUserDto);

        assertEquals(testUserDto, result);
        verify(userMapper).updateEntity(testUserDto, testUser);
    }

    @Test
    void update_whenUserNotFound_throwsNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(1L, testUserDto));
        verify(userMapper, never()).updateEntity(any(), any());
    }

    @Test
    void changeStatus_whenDifferent_updatesStatusAndReturnsDto() {
        UserDto deactivated = new UserDto(1L, "Ivan", "Petrov", LocalDate.of(1995, 5, 20),
                "ivan@mail.com", List.of(), false);
        when(repository.findById(1L)).thenReturn(Optional.of(testUser)); // active = true
        when(userMapper.toDto(testUser)).thenReturn(deactivated);

        UserDto result = userService.changeStatus(1L, false);

        assertFalse(testUser.isActive());
        assertFalse(result.active());
    }

    @Test
    void changeStatus_whenStatusAlready_throwsIllegalArgumentException(){
        when(repository.findById(1L)).thenReturn(Optional.of(testUser)); // active = true

        assertThrows(IllegalArgumentException.class,
                () -> userService.changeStatus(1L, true));
    }

    @Test
    void delete_whenUserExists_deletesUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.delete(1L);

        verify(repository).delete(testUser);
    }

    @Test
    void delete_whenUserNotFound_throwsNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(1L));
    }
}
