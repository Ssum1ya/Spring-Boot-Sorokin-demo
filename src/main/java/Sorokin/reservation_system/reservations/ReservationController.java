package Sorokin.reservation_system.reservations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping(value = "/reservation")
public class ReservationController {
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable(name = "id") Long id) {
        log.info("Called getReservationById id = " + id);
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.getReservationById(id));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(@RequestParam(value = "roomId", required = false) Long roomId,
                                                                @RequestParam(value = "userId", required = false) Long userId,
                                                                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                @RequestParam(value = "pageNumber", required = false) Integer pageNumber) {
        log.info("Called getAllReservations");
        var filter = new ReservationSearchFilter(roomId, userId, pageSize, pageNumber);
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.searchAllByFilter(filter));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody @Valid Reservation reservation) {
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(reservation));
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable(name = "id") Long id,
                                                         @RequestBody @Valid Reservation reservation) {
        log.info("Called updateReservation with id = {}, reservationToUpdate = {}", id, reservation);
        Reservation updatedReservation = reservationService.updateReservation(id, reservation);
        return ResponseEntity.status(HttpStatus.OK).body(updatedReservation);
    }

    @DeleteMapping(path = "{id}/cancel")
    public ResponseEntity<Void> deleteReservation(@PathVariable(name = "id") Long id) {
        log.info("Called deleteReservation with id = {}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "{id}")
    public ResponseEntity<Reservation> approveReservation(@PathVariable(name = "id") Long id) {
        log.info("Called approveReservation");
        Reservation reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}
