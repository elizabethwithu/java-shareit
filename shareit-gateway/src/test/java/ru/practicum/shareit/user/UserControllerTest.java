package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserClient client;

    private final UserDto userDto = new UserDto(1L, "Nick", "nick@mail.ru");

    @Test
    void createUserWithEmptyName() throws Exception {
        mockMvc.perform(post("http://localhost:8080/users")
                        .content("{" +
                                "    \"email\": \"mail@mail.ru\"" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"name\":\"Имя пользователя не указано.\"" +
                                        "   }")
                );
    }

    @Test
    void createUserWithEmptyEmail() throws Exception {
        mockMvc.perform(post("http://localhost:8080/users")
                        .content("{" +
                                "    \"name\": \"Nick\"" +
                                " }")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"email\":\"Почтовый адрес пустой.\"" +
                                        "   }")
                );
    }

    @Test
    void createUserWithFailEmail() throws Exception {
        userDto.setEmail("nick.mail.ru");

        mockMvc.perform(post("http://localhost:8080/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpectAll(
                        status().isBadRequest(),
                        MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
                        MockMvcResultMatchers.content().json(
                                "{" +
                                        "      \"email\":\"Почтовый адрес не соответствует требованиям.\"" +
                                        "   }")
                );
    }
}
