package by.mluvse.userService.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardDto(Long id,
                      @NotBlank
                      Long userId,
                      @NotBlank @Pattern(regexp = "^\\d{16}$")
                      String number,
                      @NotBlank @Pattern(regexp = "^[A-Z]+(?:\\s+[A-Z]+)*$")
                      String holder,
                      @NotBlank @Future
                      LocalDate expirationDate,
                      @NotBlank
                      boolean active) {
    public CardDto withActive(boolean active){
        return new CardDto(this.id,this.userId,this.number,this.holder,this.expirationDate,active);
    }
}
