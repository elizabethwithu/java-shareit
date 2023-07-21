package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemDao itemDao;

    private User user;
    private ItemRequest itemRequest;
    private Item item1;
    private Item item2;

    @BeforeEach
    void started() {
        user = User.builder().name("nick").email("nick@example.com").build();
        em.persist(user);

        itemRequest = ItemRequest.builder().requester(user).description("table")
                .created(LocalDateTime.of(2029, 6, 6, 6, 6)).build();
        em.persist(itemRequest);

        item1 = Item.builder().name("table").description("black")
                .owner(user).available(true).request(itemRequest).build();
        em.persist(item1);

        item2 = Item.builder().name("chair").description("white and perfect for black table")
                .owner(user).available(true).request(itemRequest).build();
        em.persist(item2);

        Item item3 = Item.builder().name("chair").description("red")
                .owner(user).available(false).build();
        em.persist(item3);
    }

    @Test
    public void contextLoads() {
        Assertions.assertThat(em).isNotNull();
    }

    @Test
    void succeedFindItemsByOwnerId() {
        List<Item> result = itemDao.findItemsByOwnerId(user.getId());

        Assertions.assertThat(result).isNotNull().hasSize(3);
    }

    @Test
    void findItemsByWrongOwnerId() {
        List<Item> result = itemDao.findItemsByOwnerId(8L);

        Assertions.assertThat(result).hasSize(0);
    }

    @Test
    void succeedFindItemsByOwnerIdPageable() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size);

        List<Item> items = itemDao.findItemsByOwnerId(user.getId(), page);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item1.getId(), items.get(0).getId());

        pageNum = 1;
        page = PageRequest.of(pageNum, size);
        items = itemDao.findItemsByOwnerId(user.getId(), page);
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @Test
    void succeedFindByRequestId() {
        List<Item> result = itemDao.findByRequestId(itemRequest.getId());

        Assertions.assertThat(result).isNotNull().hasSize(2);
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(item1, item2));
    }

    @Test
    void succeedSearchItemByText() {
        List<Item> result = itemDao.findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase("table", "table", Pageable.unpaged());

        Assertions.assertThat(result).isNotNull().hasSize(2);
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(item1, item2));
    }
}
