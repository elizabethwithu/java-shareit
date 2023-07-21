package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.constants.Request;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private static final String URL = "http://localhost:8080/bookings";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingServiceImpl bookingService;

    private BookingDto bookingDto;
    private BookingOutputDto bookingOutputDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("table")
                .description("black")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Nick")
                .email("nick@mail.ru")
                .build();

        bookingDto = BookingDto.builder()
                .id(2L)
                .itemId(1L)
                .start(now.plusYears(1))
                .end(now.plusYears(2))
                .bookerId(1L)
                .status(BookingStatus.NEW)
                .build();

        bookingOutputDto = BookingOutputDto.builder()
                .id(2L)
                .item(itemDto)
                .start(now.plusYears(1))
                .end(now.plusYears(2))
                .booker(userDto)
                .status(BookingStatus.NEW)
                .build();
    }

    @Test
    void succeedCreateBooking() throws Exception {
        when(bookingService.createBooking(any(), anyLong())).thenReturn(bookingOutputDto);

        mockMvc.perform(post(URL)
                        .header(Request.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );
    }

    @Test
    void succeedConfirmBookingByOwner() throws Exception {
        bookingOutputDto.setStatus(BookingStatus.APPROVED);
        when(bookingService.confirmBookingByOwner(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingOutputDto);

        mockMvc.perform(patch(URL + "/2")
                        .header(Request.USER_ID, 1L)
                        .param("approved", String.valueOf(true)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.status", Matchers.is(bookingOutputDto.getStatus().toString()))
                );

        bookingOutputDto.setStatus(BookingStatus.REJECTED);
        when(bookingService.confirmBookingByOwner(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingOutputDto);

        mockMvc.perform(patch(URL + "/2")
                        .header(Request.USER_ID, 1L)
                        .param("approved", String.valueOf(false)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.status", Matchers.is(bookingOutputDto.getStatus().toString()))
                );
    }

    @Test
    void succeedFindBookingById() throws Exception {
        when(bookingService.findBookingById(anyLong(), anyLong())).thenReturn(bookingOutputDto);

        mockMvc.perform(get(URL + "/2")
                        .header(Request.USER_ID, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );
    }

    @Test
    void succeedFindAllUsersBooking() throws Exception {
        //EmptyList
        when(bookingService.findAllUsersBooking(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL)
                        .header(Request.USER_ID, 1L)
                        .param("state", "rejected")
                        .param("from", "3")
                        .param("size", "3"))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json("[]")
                );
    }

    @Test
    void succeedFindAllBookingsForItems() throws Exception {
        //EmptyList
        when(bookingService.findAllBookingsForItems(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL + "/owner")
                        .header(Request.USER_ID, 1L)
                        .param("state", "rejected")
                        .param("from", "3")
                        .param("size", "3"))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json("[]")
                );
    }
}
