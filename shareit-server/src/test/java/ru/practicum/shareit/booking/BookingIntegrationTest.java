package ru.practicum.shareit.booking;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest
public class BookingIntegrationTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingDao bookingDao;

    private final LocalDateTime now = LocalDateTime.now();

    private User owner;
    private User booker;
    private Item item1;
    private Booking booking2;
    private Booking booking3;

    @BeforeEach
    void setUp() {
        owner = User.builder().name("owner").email("owner@example.com").build();
        em.persist(owner);

        booker = User.builder().name("booker").email("booker@example.com").build();
        em.persist(booker);

        item1 = Item.builder()
                .name("table").description("black").available(true)
                .owner(owner).build();
        em.persist(item1);

        Item item2 = Item.builder()
                .name("chair").description("white").available(true)
                .owner(owner).build();
        em.persist(item2);

        Booking booking1 = Booking.builder()
                .item(item1).booker(booker).status(BookingStatus.APPROVED)
                .start(now.minusDays(1)).end(now.plusDays(1))
                .build();
        em.persist(booking1);

        booking2 = Booking.builder()
                .item(item1).booker(booker).status(BookingStatus.WAITING)
                .start(now.plusDays(1)).end(now.plusDays(2))
                .build();
        em.persist(booking2);

        booking3 = Booking.builder()
                .item(item2).booker(booker).status(BookingStatus.REJECTED)
                .start(now.minusDays(2)).end(now.minusDays(1))
                .build();
        em.persist(booking3);

        Booking booking4 = Booking.builder()
                .item(item2).booker(booker).status(BookingStatus.CANCELED)
                .start(now.minusDays(1)).end(now.plusDays(1))
                .build();
        em.persist(booking4);
    }

    @Test
    void createBooking_Normal() {
        Long userId = booker.getId();
        Long itemId = item1.getId();

        BookingDto newBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(2)).end(now.plusDays(4))
                .build();

        BookingOutputDto created = bookingService.createBooking(newBooking, userId);

        Booking retrievedBooking = bookingDao.findById(created.getId()).orElse(null);
        Assertions.assertThat(retrievedBooking).isNotNull();
        Assertions.assertThat(retrievedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        Assertions.assertThat(retrievedBooking.getBooker().getId()).isEqualTo(userId);
        Assertions.assertThat(retrievedBooking.getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void approve_Normal() {
        Long userId = owner.getId();
        Long bookingId = booking2.getId();

        Assertions.assertThat(bookingService.findBookingById(booker.getId(), bookingId))
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);

        BookingOutputDto approvedBooking = bookingService.confirmBookingByOwner(userId, bookingId, true);

        Assertions.assertThat(approvedBooking).isNotNull()
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED);
    }

    @Test
    void getBooking_Normal() {
        Long userId = booker.getId();
        Long bookingId = booking3.getId();

        BookingOutputDto finder = bookingService.findBookingById(userId, bookingId);

        Assertions.assertThat(finder).isNotNull()
                .hasFieldOrPropertyWithValue("id", bookingId);
        Assertions.assertThat(finder.getBooker())
                .hasFieldOrPropertyWithValue("id", userId);
    }

    @Test
    void getAllBookings_All() {
        List<BookingOutputDto> list1 = bookingService.findAllUsersBooking(booker.getId(), State.ALL, 0, 20);
        Assertions.assertThat(list1).isNotEmpty().hasSize(4);
    }

    @Test
    void getAllBookings_Past() {
        List<BookingOutputDto> list2 = bookingService.findAllUsersBooking(booker.getId(), State.PAST, 0, 20);
        Assertions.assertThat(list2).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookings_Future() {
        List<BookingOutputDto> list3 = bookingService.findAllUsersBooking(booker.getId(), State.FUTURE, 0, 20);
        Assertions.assertThat(list3).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookings_Current() {
        List<BookingOutputDto> list4 = bookingService.findAllUsersBooking(booker.getId(), State.CURRENT, 0, 20);
        Assertions.assertThat(list4).isNotEmpty().hasSize(2);
    }

    @Test
    void getAllBookings_Rejected() {
        List<BookingOutputDto> list5 = bookingService.findAllUsersBooking(booker.getId(), State.REJECTED, 0, 20);
        Assertions.assertThat(list5).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookings_Waiting() {
        List<BookingOutputDto> list6 = bookingService.findAllUsersBooking(booker.getId(), State.WAITING, 0, 20);
        Assertions.assertThat(list6).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwner_All() {
        List<BookingOutputDto> list1 = bookingService.findAllBookingsForItems(owner.getId(), State.ALL, 0, 20);
        Assertions.assertThat(list1).isNotEmpty().hasSize(4);
    }

    @Test
    void getAllBookingsForOwner_Past() {
        List<BookingOutputDto> list2 = bookingService.findAllBookingsForItems(owner.getId(), State.PAST, 0, 20);
        Assertions.assertThat(list2).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwner_Future() {
        List<BookingOutputDto> list3 = bookingService.findAllBookingsForItems(owner.getId(), State.FUTURE, 0, 20);
        Assertions.assertThat(list3).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwner_Current() {
        List<BookingOutputDto> list4 = bookingService.findAllBookingsForItems(owner.getId(), State.CURRENT, 0, 20);
        Assertions.assertThat(list4).isNotEmpty().hasSize(2);
    }

    @Test
    void getAllBookingsForOwner_Rejected() {
        List<BookingOutputDto> list5 = bookingService.findAllBookingsForItems(owner.getId(), State.REJECTED, 0, 20);
        Assertions.assertThat(list5).isNotEmpty().hasSize(1);
    }

    @Test
    void getAllBookingsForOwner_Waiting() {
        List<BookingOutputDto> list6 = bookingService.findAllBookingsForItems(owner.getId(), State.WAITING, 0, 20);
        Assertions.assertThat(list6).isNotEmpty().hasSize(1);
    }
}
