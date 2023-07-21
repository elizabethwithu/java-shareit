package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotValidParameterException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingDao bookingDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ItemDao itemDao;

    @InjectMocks
    private BookingServiceImpl service;

    private static final LocalDateTime NOW = LocalDateTime.now();
    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingToSave;

    @BeforeEach
    void started() {
        owner = User.builder()
                .id(1L)
                .name("nick")
                .email("nick@mail.ru")
                .build();

        booker = User.builder()
                .id(2L)
                .name("fred")
                .email("fred@mail.ru")
                .build();

        item = Item.builder()
                .id(4L)
                .name("table")
                .description("red")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .start(NOW.plusDays(20))
                .end(NOW.plusDays(30))
                .status(BookingStatus.WAITING)
                .build();

        bookingToSave = BookingDto.builder()
                .itemId(item.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    @Test
    void succeedCreateBooking() {
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(bookingDao.save(any())).thenReturn(booking);

        BookingOutputDto bookingOutDto = service.createBooking(bookingToSave, booker.getId());

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

//    @Test
//    void createBookingFailByValidationPeriod() {
//        when(userDao.existsById(anyLong())).thenReturn(true);
//        bookingToSave.setStart(booking.getEnd());
//        bookingToSave.setEnd(booking.getStart());
//
//        NotValidParameterException exception = assertThrows(
//                NotValidParameterException.class,
//                () -> service.createBooking(bookingToSave, booker.getId()));
//
//        assertEquals("Дата окончания бронирования раньше даты начала или равней ей.", exception.getMessage());
//    }

    @Test
    void createBookingFailByUserNotFound() {
        long userNotFoundId = 0L;
        String error = "Пользователь с запрашиваемым айди не зарегистрирован.";
        when(userDao.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.createBooking(bookingToSave, userNotFoundId)
        );

        assertEquals(error, exception.getMessage());
        verify(bookingDao, times(0)).save(any());
    }

    @Test
    void createBookingFailByItemNotFound() {
        long itemNotFoundId = 0L;
        bookingToSave.setItemId(itemNotFoundId);

        when(userDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.findById(itemNotFoundId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.createBooking(bookingToSave, booker.getId()));

        assertEquals("Вещь с указанным айди не найдена.", exception.getMessage());
    }

    @Test
    void createBookingFailByItemNotAvailable() {
        item.setAvailable(false);
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        NotValidParameterException exception = assertThrows(
                NotValidParameterException.class,
                () -> service.createBooking(bookingToSave, booker.getId()));

        assertEquals("Вещь уже забронирована.", exception.getMessage());

        item.setAvailable(true);
        item.setOwner(booker);
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> service.createBooking(bookingToSave, booker.getId()));

        assertEquals("Владелец вещи не может её забронировать.", e.getMessage());
    }

    @Test
    void succeedConfirmBookingByOwner() {
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(bookingDao.findById(anyLong())).thenReturn(Optional.ofNullable(booking));
        when(bookingDao.save(any())).thenReturn(booking);
        when(itemDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.getReferenceById(anyLong())).thenReturn(item);

        BookingOutputDto bookingOutDto = service.confirmBookingByOwner(owner.getId(), booking.getId(), true);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
        assertEquals(BookingStatus.APPROVED, bookingOutDto.getStatus());

        bookingOutDto = service.confirmBookingByOwner(owner.getId(), booking.getId(), false);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
        assertEquals(BookingStatus.REJECTED, bookingOutDto.getStatus());
    }

    @Test
    void confirmBookingByOwnerFailByBookingNotFound() {
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(bookingDao.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.confirmBookingByOwner(owner.getId(), booking.getId(), true)
        );

        assertEquals("Бронирование с указанным айди не найдено.", exception.getMessage());
        verify(bookingDao, times(0)).save(any());
    }

    @Test
    void confirmBookingByOwnerFailByNotValidParameter() {
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(bookingDao.findById(anyLong())).thenReturn(Optional.of(booking));
        when(itemDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.getReferenceById(anyLong())).thenReturn(item);

        booking.setStatus(BookingStatus.REJECTED);
        NotValidParameterException exception = assertThrows(
                NotValidParameterException.class,
                () -> service.confirmBookingByOwner(owner.getId(), booking.getId(), false)
        );

        assertEquals("Бронирование уже отклонено.", exception.getMessage());
        verify(bookingDao, times(0)).save(any());

        booking.setStatus(BookingStatus.APPROVED);
        NotValidParameterException ex = assertThrows(
                NotValidParameterException.class,
                () -> service.confirmBookingByOwner(owner.getId(), booking.getId(), true)
        );

        assertEquals("Бронирование уже подтверждено.", ex.getMessage());
        verify(bookingDao, times(0)).save(any());
    }

    @Test
    void succeedFindBookingById() {
        when(bookingDao.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingOutputDto bookingOutDto = service.findBookingById(owner.getId(), booker.getId());

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void findBookingByIdFailByBookingNotFound() {
        when(bookingDao.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findBookingById(owner.getId(), booker.getId())
        );

        assertEquals("Бронирование с указанным айди не найдено.", exception.getMessage());
    }

    @Test
    void findBookingByIdFailByItemNotAvailable() {
        when(bookingDao.findById(anyLong())).thenReturn(Optional.ofNullable(booking));

        NotAccessException exception = assertThrows(
                NotAccessException.class,
                () -> service.findBookingById(0L, booker.getId())
        );

        assertEquals("Получение данных доступно либо автору бронирования, либо владельцу вещи",
                exception.getMessage());
    }

    @Test
    void succeedFindAllUsersBooking() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        when(userDao.existsById(anyLong())).thenReturn(true);

        //State All
        when(bookingDao.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        List<BookingOutputDto> bookingOutDto = service.findAllUsersBooking(userId, State.ALL, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        assertEquals(booking.getId(), bookingOutDto.get(0).getId());

        //State CURRENT
        booking.setEnd(NOW.plusSeconds(120));
        when(bookingDao.findByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingOutDto = service.findAllUsersBooking(userId, State.CURRENT, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //State PAST
        when(bookingDao.findByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllUsersBooking(userId, State.PAST, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //State FUTURE
        booking.setStart(NOW.plusSeconds(60));
        when(bookingDao.findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllUsersBooking(userId, State.FUTURE, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //STATE WAITING
        booking.setStatus(BookingStatus.WAITING);
        when(bookingDao.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllUsersBooking(userId, State.WAITING, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //SATE REJECTED
        booking.setStatus(BookingStatus.REJECTED);

        when(bookingDao.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllUsersBooking(userId, State.REJECTED, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
    }

    @Test
    void succeedFindAllBookingsForItems() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.findItemsByOwnerId(anyLong())).thenReturn(List.of(item));

        //State All
        when(bookingDao.findByItemOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> bookingOutDto = service.findAllBookingsForItems(userId, State.ALL, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
        assertEquals(booking.getId(), bookingOutDto.get(0).getId());

        //State CURRENT
        booking.setEnd(NOW.plusSeconds(120));
        when(bookingDao.findByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingOutDto = service.findAllBookingsForItems(userId, State.CURRENT, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //State PAST
        when(bookingDao.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllBookingsForItems(userId, State.PAST, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //State FUTURE
        booking.setStart(NOW.plusSeconds(60));
        when(bookingDao.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllBookingsForItems(userId, State.FUTURE, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //State WAITING
        booking.setStatus(BookingStatus.WAITING);
        when(bookingDao.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllBookingsForItems(userId, State.WAITING, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());

        //State REJECTED
        booking.setStatus(BookingStatus.REJECTED);

        when(bookingDao.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));

        bookingOutDto = service.findAllBookingsForItems(userId, State.REJECTED, from, size);

        assertNotNull(bookingOutDto);
        assertEquals(1, bookingOutDto.size());
    }

    @Test
    void findAllBookingsForItemsFailByUserWithoutItems() {
        String error = "У пользователя нет вещей.";
        when(userDao.existsById(anyLong())).thenReturn(true);
        when(itemDao.findItemsByOwnerId(anyLong())).thenThrow(new NotFoundException(error));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findAllBookingsForItems(owner.getId(), State.ALL, 0, 1)
        );

        assertEquals(error, exception.getMessage());
    }
}
