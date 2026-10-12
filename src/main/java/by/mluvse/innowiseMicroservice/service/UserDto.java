package by.mluvse.innowiseMicroservice.service;

import java.time.LocalDate;
import java.util.List;

public record UserDto(Long id,
                      String name,
                      String surname,
                      LocalDate birthDate,
                      String email,
                      boolean active, List<CardDto> cards) {
}
