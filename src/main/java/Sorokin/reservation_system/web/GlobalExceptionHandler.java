package Sorokin.reservation_system.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        logger.error("Handle Exception: ", e);
        var errorDto = new ErrorResponseDTO("Internal server error", LocalDateTime.now(), e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException entityNotFoundException) {
        logger.error("Handle EntityNotFoundException: ", entityNotFoundException);
        var errorDto = new ErrorResponseDTO("Entity not found", LocalDateTime.now(), entityNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(exception = {IllegalArgumentException.class,
                                    IllegalStateException.class,
                                    MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(Exception e) {
        logger.error("Handle BadRequest: ", e);
        var errorDto = new ErrorResponseDTO("Bad request", LocalDateTime.now(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }
}
