package by.mluvse.innowiseMicroservice.service;

import java.time.LocalDate;

public record CardDto(Long id,
                      Long userId,
                      String number,
                      String holder,
                      LocalDate expirationDate,
                      boolean active) {
}
