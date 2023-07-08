package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Transactional
@SpringBootTest
public class UserIntegrationTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().name("user").email("user@example.com").build();
        em.persist(user1);
        user2 = User.builder().name("admin").email("admin@example.com").build();
        em.persist(user2);
    }

    @Test
    void createUser() {
        UserDto userDto = UserDto.builder().name("owner").email("owner@example.com").build();

        UserDto saved = userService.createUser(userDto);

        Assertions.assertThat(saved).isNotNull()
                .hasFieldOrPropertyWithValue("name", "owner")
                .hasFieldOrPropertyWithValue("email", "owner@example.com")
                .hasFieldOrProperty("id")
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void findUserById() {
        Long userId = user1.getId();

        UserDto returned = userService.findUserById(userId);

        Assertions.assertThat(returned).isNotNull()
                .hasFieldOrPropertyWithValue("name", "user")
                .hasFieldOrPropertyWithValue("email", "user@example.com")
                .hasFieldOrPropertyWithValue("id", userId)
                .hasNoNullFieldsOrProperties();
    }

    @Test
    void deleteUser() {
        Long userId = user1.getId();

        long beforeDelete = userDao.count();
        Assertions.assertThat(beforeDelete).isEqualTo(2);

        userService.removeUserById(userId);

        long afterDelete = userDao.count();
        Assertions.assertThat(afterDelete).isEqualTo(1);

        Optional<User> user = userDao.findById(userId);
        Assertions.assertThat(user)
                .isNotPresent();
    }

    @Test
    void findAll() {
        List<UserDto> list = userService.findAll();

        Assertions.assertThat(list)
                .isNotEmpty()
                .hasSize(2)
                .usingRecursiveComparison()
                .comparingOnlyFields("id", "name", "email")
                .isEqualTo(List.of(user1, user2));
    }
}