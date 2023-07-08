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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemServiceImpl itemService;

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

        mockMvc.perform(post("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L)
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
    void createItemWithEmptyName() throws Exception {
        mockMvc.perform(post("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content("{" +
                                "    \"description\": \"black\"," +
                                "    \"available\": true" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"name\":\"Наименование элемента отсутствует.\"" +
                                        "   }")
                );
    }

    @Test
    void createItemWithEmptyDescription() throws Exception {
        mockMvc.perform(post("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content("{" +
                                "    \"name\": \"table\"," +
                                "    \"available\": true" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"description\":\"Описание элемента пустое.\"" +
                                        "   }")
                );
    }

    @Test
    void createItemWithEmptyAvailable() throws Exception {
        mockMvc.perform(post("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content("{" +
                                "    \"description\": \"black\"," +
                                "    \"name\": \"table\"" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"available\":\"Доступность вещи не указана.\"" +
                                        "   }")
                );
    }

    @Test
    void succeedUpdateItem() throws Exception {
        when(itemService.updateItem(any(), anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch("http://localhost:8080/items/1")
                        .header("X-Sharer-User-Id", 1L)
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

        mockMvc.perform(get("http://localhost:8080/items/2")
                        .header("X-Sharer-User-Id", 1L))
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

        mockMvc.perform(get("http://localhost:8080/items/2"))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void succeedFindAllWithDefaultParam() throws Exception {
        List<ItemDtoByOwner> items = List.of(itemDtoByOwner, itemDtoByOwner, itemDtoByOwner);

        when(itemService.findAll(anyLong(), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(itemDtoByOwner.getId()), Long.class),
                        jsonPath("$[0].name", Matchers.is(itemDtoByOwner.getName())),
                        jsonPath("$[0].description", Matchers.is(itemDtoByOwner.getDescription())),
                        jsonPath("$[0].available", Matchers.is(itemDtoByOwner.getAvailable())),
                        jsonPath("$.length()", Matchers.is(3))
                );
    }

    @Test
    void succeedFindAllWithWrongParam() throws Exception {
        List<ItemDtoByOwner> items = List.of(itemDtoByOwner, itemDtoByOwner, itemDtoByOwner);

        when(itemService.findAll(anyLong(), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                        "{" +
                                "      \"Некорректное значение\":\"findAll.from: must be greater than or equal to 0\"" +
                                "   }")
                );

        mockMvc.perform(get("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "0"))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                        "{" +
                                "      \"Некорректное значение\":\"findAll.size: must be greater than or equal to 1\"" +
                                "   }")
                );
    }

    @Test
    void succeedFindItemByDescriptionWithDefaultParams() throws Exception {
        when(itemService.findItemByDescription(any(), anyInt(), anyInt())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("http://localhost:8080/items/search")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id", Matchers.is(itemDto.getId()), Long.class),
                        jsonPath("$[0].name", Matchers.is(itemDto.getName())),
                        jsonPath("$[0].description", Matchers.is(itemDto.getDescription())),
                        jsonPath("$[0].available", Matchers.is(itemDto.getAvailable())),
                        jsonPath("$.length()", Matchers.is(1))
                );
    }

    @Test
    void findItemByDescriptionWithWrongParams() throws Exception {
        when(itemService.findItemByDescription(any(), anyInt(), anyInt())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("http://localhost:8080/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":\"findItemByDescription.from: must be greater than or equal to 0\"" +
                                        "   }")
                );

        mockMvc.perform(get("http://localhost:8080/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "0"))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"Некорректное значение\":\"findItemByDescription.size: must be greater than or equal to 1\"" +
                                        "   }")
                );
    }

    @Test
    void succeedAddComment() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);

        mockMvc.perform(post("http://localhost:8080/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentWithoutText() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);

        mockMvc.perform(post("http://localhost:8080/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content("{" +
                                "    \"authorName\": \"nick\"," +
                                "    \"itemId\": 2" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"text\":\"Текст комментария отсутствует.\"" +
                                        "   }")
                );
    }

    @Test
    void addCommentWithoutSharerUserId() throws Exception {
        when(itemService.addComment(any(), anyInt(), anyInt())).thenReturn(commentDto);

        mockMvc.perform(post("http://localhost:8080/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void succeedDeleteItemById() throws Exception {
        mockMvc.perform(delete("http://localhost:8080/items/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemService, times(1))
                .removeItemById(anyLong(), anyLong());
    }

    @Test
    void deleteItemByIdWithoutSharerUserId() throws Exception {
        mockMvc.perform(delete("http://localhost:8080/items/2"))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0))
                .removeItemById(anyLong(), anyLong());
    }
}
