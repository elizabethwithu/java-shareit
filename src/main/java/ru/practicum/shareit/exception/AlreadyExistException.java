package ru.practicum.shareit.exception;

public class AlreadyExistException extends RuntimeException {
    private static final String message = "%s уже зарегистрирован.";

    public AlreadyExistException(String s) {
        super(
                String.format(message, s)
        );
    }
}
