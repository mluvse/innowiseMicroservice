package by.mluvse.innowiseMicroservice.exceptions;

public class CardLimitException extends RuntimeException {
    public CardLimitException(String message) {
        super(message);
    }
}
