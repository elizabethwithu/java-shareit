package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingDao extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                              LocalDateTime end);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                                 LocalDateTime end);

    Booking findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(BookingStatus status, Long authorId, Long itemId);

    List<Booking> findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(Long itemId, Long userId, LocalDateTime start,
                                                                    BookingStatus status);
    List<Booking> findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(Long itemId, Long userId, LocalDateTime start,
                                                                    BookingStatus status);
}
