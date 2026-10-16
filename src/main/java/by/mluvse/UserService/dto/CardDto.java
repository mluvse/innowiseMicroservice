package by.mluvse.UserService.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.time.LocalDate;

public record CardDto(Long id,
                      @NotNull
                      Long userId,
                      @NotBlank @Pattern(regexp = "^\\d{13,19}$")
                      String number,
                      @NotBlank @Pattern(regexp = "^[A-Z]+(?:\\s+[A-Z]+)*$")
                      String holder,
                      @NotNull @Future
                      LocalDate expirationDate,
                      boolean active) implements Serializable {
    public void withActive(boolean active){
        new CardDto(this.id, this.userId, this.number, this.holder, this.expirationDate, active);
    }
}
