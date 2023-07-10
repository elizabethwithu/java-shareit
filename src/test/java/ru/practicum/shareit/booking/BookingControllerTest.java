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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

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

        mockMvc.perform(post("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1L)
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
    void createBookingWithoutBookingId() throws Exception {
        bookingDto.setItemId(null);
        String error = "Элемент бронирования отсутствует.";

        mockMvc.perform(post("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.itemId", containsString(error), String.class));
    }

    @Test
    void createBookingFailStartTime() throws Exception {
        bookingDto.setStart(LocalDateTime.of(2020, 9, 9, 15, 15));
        String error = "Дата начала бронирования указана в прошлом.";

        mockMvc.perform(post("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.start", containsString(error), String.class));

        bookingDto.setStart(null);
        error = "Дата начала бронирования не указана.";

        mockMvc.perform(post("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.start", containsString(error), String.class));
    }

    @Test
    void createBookingFailEndTime() throws Exception {
        bookingDto.setEnd(LocalDateTime.of(2020, 9, 9, 15, 15));
        String error = "Дата окончания бронирования указана в прошлом.";

        mockMvc.perform(post("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.end", containsString(error), String.class));

        bookingDto.setEnd(null);
        error = "Дата окончания бронирования не указана.";

        mockMvc.perform(post("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.end", containsString(error), String.class));
    }

    @Test
    void succeedConfirmBookingByOwner() throws Exception {
        bookingOutputDto.setStatus(BookingStatus.APPROVED);
        when(bookingService.confirmBookingByOwner(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingOutputDto);

        mockMvc.perform(patch("http://localhost:8080/bookings/2")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", String.valueOf(true)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.status", Matchers.is(bookingOutputDto.getStatus().toString()))
                );

        bookingOutputDto.setStatus(BookingStatus.REJECTED);
        when(bookingService.confirmBookingByOwner(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingOutputDto);

        mockMvc.perform(patch("http://localhost:8080/bookings/2")
                        .header("X-Sharer-User-Id", 1L)
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

        mockMvc.perform(get("http://localhost:8080/bookings/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$.item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$.booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );
    }

    @Test
    void succeedFindAllUsersBooking() throws Exception {
        when(bookingService.findAllUsersBooking(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutputDto));

        mockMvc.perform(get("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$[0].item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$[0].booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );

        //EmptyList
        when(bookingService.findAllUsersBooking(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "rejected"))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json("[]")
                );
    }

    @Test
    void findAllUsersBookingFailByIncorrectFrom() throws Exception {
        mockMvc.perform(get("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"findAllUsersBooking.from: must be greater than or equal to 0\"" +
                                        "   }"));
    }

    @Test
    void findAllUsersBookingFailByIncorrectSize() throws Exception {
        mockMvc.perform(get("http://localhost:8080/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"findAllUsersBooking.size: must be greater than or equal to 1\"" +
                                        "   }"));
    }

    @Test
    void succeedFindAllBookingsForItems() throws Exception {
        when(bookingService.findAllBookingsForItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingOutputDto));

        mockMvc.perform(get("http://localhost:8080/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(bookingOutputDto.getId()), Long.class),
                        jsonPath("$[0].item.id", Matchers.is(bookingOutputDto.getItem().getId()), Long.class),
                        jsonPath("$[0].booker.id", Matchers.is(bookingOutputDto.getBooker().getId()), Long.class)
                );

        //EmptyList
        when(bookingService.findAllBookingsForItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("http://localhost:8080/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "rejected"))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json("[]")
                );
    }

    @Test
    void findAllBookingsForItemsFailByIncorrectFrom() throws Exception {
        mockMvc.perform(get("http://localhost:8080/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"findAllBookingsForItems.from: must be greater than or equal to 0\"" +
                                        "   }"));
    }

    @Test
    void findAllBookingsForItemsFailByIncorrectSize() throws Exception {
        mockMvc.perform(get("http://localhost:8080/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpectAll(status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":" +
                                        "\"findAllBookingsForItems.size: must be greater than or equal to 1\"" +
                                        "   }"));
    }
}
