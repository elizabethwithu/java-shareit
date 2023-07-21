package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.constants.Request;
import ru.practicum.shareit.exception.NotValidParameterException;
import ru.practicum.shareit.exception.UnsupportedStateException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader(Request.USER_ID) long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@RequestParam(defaultValue = "0", required = false) @Min(0) Integer from,
			@RequestParam(defaultValue = "10", required = false) @Min(1) Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new UnsupportedStateException("Unknown state: UNSUPPORTED_STATUS"));
		return bookingClient.getBookings(userId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> bookItem(@RequestHeader(Request.USER_ID) long userId,
			@RequestBody @Valid BookingDto requestDto) {
		LocalDateTime end = requestDto.getEnd();
		LocalDateTime start = requestDto.getStart();
		if (!end.isAfter(start) || end.equals(start)) {
			throw new NotValidParameterException("Дата окончания бронирования раньше даты начала или равней ей.");
		}
		return bookingClient.bookItem(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(Request.USER_ID) long userId,
			@PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> findAllBookingsForItems(@RequestHeader(Request.USER_ID) Long userId,
			@RequestParam(defaultValue = "ALL", required = false) String state,
			@RequestParam(defaultValue = "0", required = false) @Min(0) int from,
			@RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
		BookingState bookingState = BookingState.from(state)
				.orElseThrow(() -> new UnsupportedStateException("Unknown state: UNSUPPORTED_STATUS"));
		return bookingClient.findAllBookingsForItems(userId, bookingState, from, size);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> confirmBookingByOwner(@RequestHeader(Request.USER_ID) Long userId,
												  @PathVariable Long bookingId, @RequestParam Boolean approved) {
		return bookingClient.confirmBookingByOwner(userId, bookingId, approved);
	}
}