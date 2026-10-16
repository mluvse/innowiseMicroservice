package by.mluvse.UserService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record UserDto(

        Long id,
        @NotBlank
        @Size(max = 250)
        String name,
        @NotBlank @Size(max = 250)
        String surname,
        @NotNull @Past
        LocalDate birthDate,
        @NotBlank @Email
        String email,
        @Valid @Size(max = 5)
        List<CardDto> cards,
        boolean active)  implements Serializable {
        public void withActive(boolean active){
                new UserDto(this.id, this.name, this.surname, this.birthDate, this.email, this.cards, active);
        }
}
