package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;

import java.util.List;

public interface BookingService {
    BookingOutputDto createBooking(BookingDto dto, Long userId);

    BookingOutputDto confirmBookingByOwner(Long userId, Long bookingId, boolean approved);

    BookingOutputDto findBookingById(Long userId, Long bookingId);

    List<BookingOutputDto> findAllUsersBooking(Long userId, String state);

    List<BookingOutputDto> findAllBookingsForItems(Long userId, String state);
}
