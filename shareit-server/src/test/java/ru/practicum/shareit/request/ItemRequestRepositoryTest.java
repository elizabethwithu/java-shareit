package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class ItemRequestRepositoryTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRequestDao itemRequestDao;

    private User user;
    private ItemRequest itemRequest;
    private User user2;
    private ItemRequest itemRequest2;

    @BeforeEach
    void started() {
        user = User.builder()
                .name("nick")
                .email("nick@example.com")
                .build();

        itemRequest = ItemRequest.builder()
                .requester(user)
                .description("table")
                .created(LocalDateTime.of(2029, 6, 6, 6, 6))
                .build();

        user2 = User.builder()
                .name("mike")
                .email("mike@example.com")
                .build();

        itemRequest2 = ItemRequest.builder()
                .requester(user2)
                .description("table")
                .created(LocalDateTime.of(2029, 6, 6, 6, 6))
                .build();
    }

    @Test
    void succeedFindAllByRequesterId() {
        em.persist(user);
        em.persist(itemRequest);
        em.persist(user2);
        em.persist(itemRequest2);
        List<ItemRequest> result = itemRequestDao.findAllByRequesterId(user.getId());

        Assertions.assertThat(result).isNotNull().hasSize(1);
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(itemRequest));
    }

    @Test
    void findAllByWrongRequesterIdEmptyList() {
        em.persist(user);
        em.persist(itemRequest);
        em.persist(user2);
        em.persist(itemRequest2);
        List<ItemRequest> result = itemRequestDao.findAllByRequesterId(15L);

        Assertions.assertThat(result).isNotNull().hasSize(0);
    }

    @Test
    void succeedFindAllByRequesterIdNot() {
        em.persist(user);
        em.persist(itemRequest);
        em.persist(user2);
        em.persist(itemRequest2);
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size, SORT);

        List<ItemRequest> result = itemRequestDao.findAllByRequesterIdNot(user.getId(), page);
        Assertions.assertThat(result).isNotNull().hasSize(1);
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(itemRequest2));
    }

    @Test
    void findAllByWrongRequesterIdNotReturnEmptyList() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size, SORT);

        List<ItemRequest> result = itemRequestDao.findAllByRequesterIdNot(1L, page);
        Assertions.assertThat(result).isNotNull().hasSize(0);
    }
}
