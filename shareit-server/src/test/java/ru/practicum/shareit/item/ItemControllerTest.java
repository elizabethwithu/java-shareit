package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.constants.Request;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    private static final String URL = "http://localhost:8080/items";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemServiceImpl itemService;

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("table")
            .description("black")
            .available(true)
            .build();
    private final BookingDto bookingDto = new BookingDto();
    private final ItemDtoByOwner itemDtoByOwner = ItemDtoByOwner.builder()
            .id(2L)
            .name("table")
            .description("black")
            .available(true)
            .lastBooking(bookingDto)
            .nextBooking(bookingDto)
            .comments(List.of(new CommentDto()))
            .build();
    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("hey")
            .authorName("nick")
            .itemId(2L)
            .build();

    @AfterEach
    void deleteUser() {
        itemService.removeItemById(anyLong(), anyLong());
    }

    @Test
    void succeedCreateItem() throws Exception {
        when(itemService.createItem(any(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post(URL)
                        .header(Request.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(itemDto.getName())),
                        jsonPath("$.description", Matchers.is(itemDto.getDescription())),
                        jsonPath("$.available", Matchers.is(itemDto.getAvailable()))
                );
    }

    @Test
    void succeedUpdateItem() throws Exception {
        when(itemService.updateItem(any(), anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch(URL + "/1")
                        .header(Request.USER_ID, 1L)
                        .content("{" +
                                "    \"name\": \"table\"," +
                                "    \"description\": \"black\"" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(itemDto.getName())),
                        jsonPath("$.description", Matchers.is(itemDto.getDescription())),
                        jsonPath("$.available", Matchers.is(itemDto.getAvailable()))
                );
    }

    @Test
    void succeedFindByIdItem() throws Exception {
        when(itemService.findItemById(anyLong(), anyLong())).thenReturn(itemDtoByOwner);

        mockMvc.perform(get(URL + "/2")
                        .header(Request.USER_ID, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemDtoByOwner.getId()), Long.class),
                        jsonPath("$.name", Matchers.is(itemDtoByOwner.getName())),
                        jsonPath("$.description", Matchers.is(itemDtoByOwner.getDescription())),
                        jsonPath("$.available", Matchers.is(itemDtoByOwner.getAvailable()))
                );
    }

    @Test
    void findByIdItemWithoutSharerUserId() throws Exception {
        when(itemService.findItemById(anyLong(), anyLong())).thenReturn(itemDtoByOwner);

        mockMvc.perform(get(URL + "/2"))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void succeedAddComment() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);

        mockMvc.perform(post(URL + "/1/comment")
                        .header(Request.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentWithoutSharerUserId() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);

        mockMvc.perform(post(URL + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void succeedDeleteItemById() throws Exception {
        mockMvc.perform(delete(URL + "/2")
                        .header(Request.USER_ID, 1L))
                .andExpect(status().isOk());

        verify(itemService, times(1))
                .removeItemById(anyLong(), anyLong());
    }

    @Test
    void deleteItemByIdWithoutSharerUserId() throws Exception {
        mockMvc.perform(delete(URL + "/2"))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0))
                .removeItemById(anyLong(), anyLong());
    }
}
