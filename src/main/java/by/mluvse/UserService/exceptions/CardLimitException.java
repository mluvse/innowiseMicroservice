package by.mluvse.UserService.exceptions;

public class CardLimitException extends RuntimeException {
    public CardLimitException(String message) {
        super(message);
    }
}
