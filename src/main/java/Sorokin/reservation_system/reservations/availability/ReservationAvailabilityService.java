package Sorokin.reservation_system.reservations.availability;

import Sorokin.reservation_system.reservations.ReservationRepository;
import Sorokin.reservation_system.reservations.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {
    private final ReservationRepository reservationRepository;
    private static final Logger logger = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    public ReservationAvailabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public boolean isReservationAvailable(Long roomId, LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new IllegalStateException("Start date must be 1 day earlier than end date");
        }
        List<Long> conflictingIds = reservationRepository.findConflictReservations(roomId, startDate, endDate, ReservationStatus.APPROVED);
        if (conflictingIds.isEmpty()) {
            return false;
        }
        logger.info("Conflicting with ids = {}", conflictingIds);
        return true;
    }
}
