package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors()
                .forEach((error) -> {
                    FieldError fieldError = ((FieldError) error);
                    String fieldName = fieldError.getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);

                    log.error(errorMessage + " " + fieldError.getRejectedValue(), fieldName);
                });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFoundException(NotFoundException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Объект не найден", e.getMessage());

        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());

        return ResponseEntity.status(404).body(errors);
    }

    @ExceptionHandler(NotValidParameterException.class)
    public ResponseEntity<?> notValidIdException(NotValidParameterException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Некорректное значение", e.getMessage());

        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());

        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<?> alreadyExistException(AlreadyExistException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Похожий объект уже существует", e.getMessage());

        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());

        return ResponseEntity.status(409).body(errors);
    }

    @ExceptionHandler(NotAccessException.class)
    public ResponseEntity<?> notAccessException(NotAccessException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Объект не доступен для редактирования", e.getMessage());

        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());

        return ResponseEntity.status(404).body(errors);
    }

    @ExceptionHandler(UnsupportedStateException.class)
    public ResponseEntity<?> unsupportedStateException(UnsupportedStateException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", e.getMessage());

        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());

        return ResponseEntity.status(500).body(errors);
    }
}
