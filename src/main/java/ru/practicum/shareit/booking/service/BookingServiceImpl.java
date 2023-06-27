package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotValidParameterException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.user.service.UserService.checkUserAvailability;
import static ru.practicum.shareit.item.service.ItemService.checkItemAvailability;
import static ru.practicum.shareit.item.service.ItemService.checkItemAccess;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingDao bookingDao;
    private final UserDao userDao;
    private final ItemDao itemDao;

    @Transactional
    @Override
    public BookingOutputDto createBooking(BookingDto dto, Long userId) {
        Long itemId = dto.getItemId();
        validationBookingPeriod(dto);
        checkUserAvailability(userDao, userId);

        Item item = itemDao.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с указанным айди не найдена."));
        if (!item.getAvailable()) {
            throw new NotValidParameterException("Вещь уже забронирована.");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Владелец вещи не может её забронировать.");
        }
        dto.setStatus(BookingStatus.WAITING);
        Booking booking = BookingMapper.toBooking(dto, item, userDao.getReferenceById(userId));
        return BookingMapper.doBookingOutputDto(bookingDao.save(booking));
    }

    @Transactional
    @Override
    public BookingOutputDto confirmBookingByOwner(Long userId, Long bookingId, boolean approved) {
        checkUserAvailability(userDao, userId);
        Booking booking = bookingDao.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с указанным айди не найдено."));
        Long itemId = booking.getItem().getId();
        checkItemAvailability(itemDao, itemId);
        checkItemAccess(itemDao, userId, itemId);

        if (approved && booking.getStatus() == BookingStatus.APPROVED) {
            throw new NotValidParameterException("Бронирование уже подтверждено.");
        }
        if (!approved && booking.getStatus() == BookingStatus.REJECTED) {
            throw new NotValidParameterException("Бронирование уже отклонено.");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.doBookingOutputDto(bookingDao.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingOutputDto findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingDao.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с указанным айди не найдено."));

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        boolean checkOwnerOrBooker = ownerId.equals(userId) || bookerId.equals(userId);
        if (!checkOwnerOrBooker) {
            throw new NotAccessException("Получение данных доступно либо автору бронирования, либо владельцу вещи");
        }
        return BookingMapper.doBookingOutputDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutputDto> findAllUsersBooking(Long userId, String state) {
        checkUserAvailability(userDao, userId);
        LocalDateTime start = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        checkEnumExist(state);
        State bookingStatus = State.valueOf(state.toUpperCase());

        switch (bookingStatus) {
            case ALL:
                bookings = bookingDao.findByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingDao.findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, start, start);
                break;
            case PAST:
                bookings = bookingDao.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId, start);
                break;
            case FUTURE:
                bookings = bookingDao.findByBookerIdAndStartIsAfterOrderByStartDesc(userId, start);
                break;
            case WAITING:
                bookings = bookingDao.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingDao.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
        }
        return BookingMapper.makeBookingsOutputList(bookings);
    }

    @Override
    public List<BookingOutputDto> findAllBookingsForItems(Long userId, String state) {
        checkUserAvailability(userDao, userId);
        if (itemDao.findItemsByOwnerId(userId).isEmpty()) {
            throw new NotFoundException("У пользователя нет вещей.");
        }
        LocalDateTime start = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        checkEnumExist(state);
        State bookingStatus = State.valueOf(state.toUpperCase());

        switch (bookingStatus) {
            case ALL:
                bookings = bookingDao.findByItemOwnerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingDao.findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, start, start);
                break;
            case PAST:
                bookings = bookingDao.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId, start);
                break;
            case FUTURE:
                bookings = bookingDao.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId, start);
                break;
            case WAITING:
                bookings = bookingDao.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingDao.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
        }
        return BookingMapper.makeBookingsOutputList(bookings);
    }

    private void validationBookingPeriod(BookingDto booking) {
        LocalDateTime end = booking.getEnd();
        LocalDateTime start = booking.getStart();
        if (!end.isAfter(start) || end.equals(start)) {
            throw new NotValidParameterException("Дата окончания бронирования раньше даты начала или равней ей.");
        }
    }

    private void checkEnumExist(String state) {
        for (State available : State.values()) {
            if (available.name().equals(state)) {
                return;
            }
        }
        throw new UnsupportedStateException("Unknown state: UNSUPPORTED_STATUS");
    }
}
