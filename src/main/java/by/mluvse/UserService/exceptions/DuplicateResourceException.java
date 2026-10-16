package by.mluvse.UserService.exceptions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(@NotBlank @Pattern(regexp = "^\\d{16}$") String s) {
    }
}
