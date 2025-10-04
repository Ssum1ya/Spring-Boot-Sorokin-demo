package Sorokin.reservation_system.reservations;

import Sorokin.reservation_system.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationMapper reservationMapper;
    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationMapper reservationMapper,
                              ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.reservationAvailabilityService = reservationAvailabilityService;
    }


    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));


        return reservationMapper.toReservation(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(ReservationSearchFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<ReservationEntity> reservationEntities = reservationRepository.searchAllByFilter(filter.roomId(), filter.userId(), pageable);
        return reservationEntities.stream().map(reservationMapper::toReservation).toList();
    }

    public Reservation create(Reservation reservation) {
        if (reservation.getReservationStatus() != null) {
            throw new IllegalArgumentException("id should be empty");
        }
        if (!reservation.getEndDate().isAfter(reservation.getStartDate())) {
            throw new IllegalStateException("Start date must be 1 day earlier than end date");
        }

        ReservationEntity reservationEntityToSave = reservationMapper.toEntity(reservation);
        reservationEntityToSave.setReservationStatus(ReservationStatus.PENDING);

        var savedEntity = reservationRepository.save(reservationEntityToSave);
        return reservationMapper.toReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation updatedReservation) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getReservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status=" + reservationEntity.getReservationStatus());
        }
        if (!updatedReservation.getEndDate().isAfter(updatedReservation.getStartDate())) {
            throw new IllegalStateException("Start date must be 1 day earlier than end date");
        }

        ReservationEntity reservationEntityUpdate = reservationMapper.toEntity(updatedReservation);
        reservationEntityUpdate.setId(reservationEntity.getId());
        reservationEntityUpdate.setReservationStatus(ReservationStatus.PENDING);

        var savedReservationEntity = reservationRepository.save(reservationEntityUpdate);
        return reservationMapper.toReservation(savedReservationEntity);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        if (reservation.getReservationStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager");
        }
        if (reservation.getReservationStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel the reservation reservation has already cancelled");
        }
        reservationRepository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id = {}", id);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getReservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("cannot approve reservation: status=" + reservationEntity.getReservationStatus());
        }

        boolean isConflict = reservationAvailabilityService.isReservationAvailable(reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate());
        if (isConflict) {
            throw new IllegalStateException("cannot approve reservation because of conflict");
        }

        reservationEntity.setReservationStatus(ReservationStatus.APPROVED);
        var updatedReservationEntity = reservationRepository.save(reservationEntity);
        return reservationMapper.toReservation(updatedReservationEntity);
    }
}
