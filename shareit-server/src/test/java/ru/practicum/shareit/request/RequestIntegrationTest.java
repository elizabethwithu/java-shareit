package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest
public class RequestIntegrationTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private ItemRequestService itemRequestService;

    private User owner;
    private User user;
    private ItemRequest request;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("user")
                .email("user@example.com")
                .build();
        em.persist(user);
        owner = User.builder()
                .name("owner")
                .email("owner@example.com")
                .build();
        em.persist(owner);
        request = ItemRequest.builder()
                .description("table")
                .created(now.minusHours(3))
                .requester(user)
                .build();
        em.persist(request);
        ItemRequest request2 = ItemRequest.builder()
                .description("chair")
                .created(now.minusHours(6))
                .requester(user)
                .build();
        em.persist(request2);
    }

    @Test
    void createRequest() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("chair")
                .build();

        ItemRequestDto saved = itemRequestService.createRequest(user.getId(), requestDto);

        String query = "select ir from ItemRequest as ir where ir.id=:id";

        ItemRequest result = em.createQuery(query, ItemRequest.class)
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(result, notNullValue());
        assertThat(result.getRequester().getName(), equalTo(user.getName()));
        assertThat(result.getRequester().getEmail(), equalTo(user.getEmail()));
        assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.getId(), equalTo(saved.getId()));
    }

    @Test
    void findAllUsersRequestsWithReplies() {
        List<ItemRequestDtoByOwner> list = itemRequestService.findAllUsersRequestsWithReplies(user.getId());

        Assertions.assertThat(list).isNotEmpty().hasSize(2);
    }

    @Test
    void findAll() {
        Long userId = owner.getId();

        List<ItemRequestDtoByOwner> list = itemRequestService.findAll(userId, 0, 20);

        Assertions.assertThat(list).isNotEmpty().hasSize(2);
    }

    @Test
    void findAllWithEmptyList() {
        Long userId = user.getId();

        List<ItemRequestDtoByOwner> list = itemRequestService.findAll(userId, 0, 20);

        Assertions.assertThat(list).isEmpty();
    }

    @Test
    void findByIdWithReplies() {

        ItemRequestDtoByOwner itemRequestDto = itemRequestService.findByIdWithReplies(user.getId(), request.getId());


        String query = "select ir from ItemRequest as ir where ir.id=:id";
        ItemRequest result = em.createQuery(query, ItemRequest.class)
                .setParameter("id", itemRequestDto.getId())
                .getSingleResult();

        assertThat(itemRequestDto.getCreated(), Matchers.is(now.minusHours(3)));
        assertThat(itemRequestDto.getDescription(), Matchers.is(request.getDescription()));
        assertThat(result.getRequester(), Matchers.is(user));
    }
}
