package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.constants.Request;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    private static final String URL = "http://localhost:8080/requests";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestServiceImpl itemRequestService;

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

        mockMvc.perform(post(URL)
                        .header(Request.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemRequestDto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemRequestDto.getId()), Long.class),
                        jsonPath("$.description", Matchers.is(itemRequestDto.getDescription()))
        );
    }

    @Test
    void succeedFindAllWithReplies() throws Exception {
        when(itemRequestService.findAllUsersRequestsWithReplies(anyLong())).thenReturn(List.of(itemRequestDtoByOwner));

        mockMvc.perform(get(URL)
                        .header(Request.USER_ID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void succeedFindById() throws Exception {
        when(itemRequestService.findByIdWithReplies(anyLong(), anyLong())).thenReturn(itemRequestDtoByOwner);

        mockMvc.perform(get(URL + "/1")
                        .header(Request.USER_ID, 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", Matchers.is(itemRequestDto.getId()), Long.class),
                        jsonPath("$.description", Matchers.is(itemRequestDto.getDescription()))
                );
    }
}
