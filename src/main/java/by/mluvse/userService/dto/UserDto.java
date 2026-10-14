package by.mluvse.userService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UserDto(

        Long id,
        @NotBlank
        @Size(max = 250)
        String name,
        @NotBlank
        String surname,
        @NotBlank @Past
        LocalDate birthDate,
        @NotBlank @Email
        String email,
        @Valid
        List<CardDto> cards) {
}
