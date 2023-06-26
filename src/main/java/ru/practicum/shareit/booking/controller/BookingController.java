package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingOutputDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @Valid @RequestBody BookingDto dto) {
        return bookingService.createBooking(dto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutputDto confirmBookingByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long bookingId, @RequestParam Boolean approved) {
        return bookingService.confirmBookingByOwner(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutputDto findBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long bookingId) {
        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutputDto> findAllUsersBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "ALL", required = false) String state) {
        return bookingService.findAllUsersBooking(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingOutputDto> findAllBookingsForItems(@RequestHeader("X-Sharer-User-Id") Long userId,
             @RequestParam(defaultValue = "ALL", required = false) String state) {
        return bookingService.findAllBookingsForItems(userId, state);
    }
}
