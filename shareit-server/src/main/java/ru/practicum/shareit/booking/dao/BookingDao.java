package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingDao extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable page);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable page);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                              LocalDateTime end, Pageable page);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long bookerId, Pageable page);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable page);

    List<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable page);

    List<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                                 LocalDateTime end, Pageable page);

    Booking findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(BookingStatus status, Long authorId, Long itemId);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId, LocalDateTime start,
                                                                    BookingStatus status, Pageable page);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId, LocalDateTime start,
                                                                          BookingStatus status);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId, LocalDateTime start,
                                                                    BookingStatus status, Pageable page);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId, LocalDateTime start,
                                                                           BookingStatus status);
}
