package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemClient client;

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
    void succeedFindAllWithWrongParam() throws Exception {
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
    void addCommentWithoutText() throws Exception {
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
    void findItemByDescriptionWithWrongParams() throws Exception {
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
    void succeedFindAllWithoutParam() throws Exception {
        mockMvc.perform(get("http://localhost:8080/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(
                        status().isOk()
                );
    }
}
