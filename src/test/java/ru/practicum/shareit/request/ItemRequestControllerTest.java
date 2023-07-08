package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemRequestServiceImpl itemRequestService;

    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("cutting board")
            .build();
    private final ItemRequestDtoByOwner itemRequestDtoByOwner = ItemRequestDtoByOwner.builder()
            .id(1L)
            .description("cutting board")
            .build();

    @Test
    void succeedCreateRequest() throws Exception {
        when(itemRequestService.createRequest(anyLong(), any())).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemRequestDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemRequestDto.getId()), Long.class),
                        jsonPath("$.description", Matchers.is(itemRequestDto.getDescription()))
        );
    }

    @Test
    void createRequestWithoutDescription() throws Exception {
        when(itemRequestService.createRequest(anyLong(), any())).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content("{" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "    \"description\": \"Описание запроса пустое.\"" +
                                        " }")
                );
    }

    @Test
    void succeedFindAllWithDefaultParams() throws Exception {
        when(itemRequestService.findAll(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemRequestDtoByOwner));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void succeedFindAllWithWrongParams() throws Exception {

        mockMvc.perform(get("/requests/all")
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

        mockMvc.perform(get("/requests/all")
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
    void succeedFindAllWithReplies() throws Exception {
        when(itemRequestService.findAllUsersRequestsWithReplies(anyLong())).thenReturn(List.of(itemRequestDtoByOwner));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void succeedFindById() throws Exception {
        when(itemRequestService.findByIdWithReplies(anyLong(), anyLong())).thenReturn(itemRequestDtoByOwner);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemRequestDto.getId()), Long.class),
                        jsonPath("$.description", Matchers.is(itemRequestDto.getDescription()))
                );
    }
}
