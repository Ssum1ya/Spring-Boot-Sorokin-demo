package Sorokin.reservation_system.reservations.availability;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {
    private final ReservationAvailabilityService reservationAvailabilityService;
    private static final Logger logger = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    public ReservationAvailabilityController(ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    @PostMapping
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(@RequestParam @Valid CheckAvailabilityRequest checkAvailabilityRequest) {
        logger.info("Called method checkAvailability: request = {}", checkAvailabilityRequest);
        boolean isAvailable = reservationAvailabilityService.isReservationAvailable(checkAvailabilityRequest.roomId(),
                checkAvailabilityRequest.startDate(),
                checkAvailabilityRequest.endDate());
        var message = isAvailable ? "Room available to reservation": "Room not available to reservation";
        var status = isAvailable ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.RESERVED;
        return ResponseEntity.status(200).body(new CheckAvailabilityResponse(message, status));
    }
}
