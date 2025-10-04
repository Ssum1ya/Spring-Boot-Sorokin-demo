package Sorokin.reservation_system.web;

import java.time.LocalDateTime;

public record ErrorResponseDTO(String message, LocalDateTime errorTime, String errorMessage) {

}
