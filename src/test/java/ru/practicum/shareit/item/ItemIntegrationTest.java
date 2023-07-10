package ru.practicum.shareit.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest
public class ItemIntegrationTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private ItemService itemService;

    private final LocalDateTime now = LocalDateTime.now();

    private User owner;
    private User booker;
    private Item item;
    private Item item2;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("owner").email("owner@example.com")
                .build();
        em.persist(owner);
        booker = User.builder()
                .name("booker").email("booker@example.com")
                .build();
        em.persist(booker);
        User booker2 = User.builder()
                .name("booker2").email("booker2@example.com")
                .build();
        em.persist(booker2);
        // Вещь пользователя owner
        item = Item.builder()
                .name("table").description("black table").available(true)
                .owner(owner)
                .build();
        em.persist(item);
        item2 = Item.builder()
                .name("chair").description("black chair").available(true)
                .owner(owner)
                .build();
        em.persist(item2);
        // Бронирование пользователем booker
        Booking booking = Booking.builder()
                .item(item).booker(booker).status(BookingStatus.APPROVED)
                .start(now.minusDays(5))
                .end(now.minusDays(4))
                .build();
        em.persist(booking);
        // Бронирование пользователем booker2
        Booking booking2 = Booking.builder()
                .item(item).booker(booker2).status(BookingStatus.APPROVED)
                .start(now.minusDays(3))
                .end(now.minusDays(2))
                .build();
        em.persist(booking2);
        // Комментарий пользователя booker2
        Comment comment2 = Comment.builder()
                .text("table for the whole family")
                .item(item)
                .created(now.minusDays(3))
                .author(booker2)
                .build();
        em.persist(comment2);
    }

    @Test
    void createItem_Normal() {
        Long userId = owner.getId();
        ItemDto newItem = ItemDto.builder()
                .name("bed").description("white")
                .available(true)
                .build();

        ItemDto saved = itemService.createItem(newItem, userId);

        Assertions.assertThat(saved).isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(newItem);
    }

    @Test
    void updateItem_Normal() {
        ItemDto updater = ItemMapper.doItemDto(item);
        updater.setName("new table");
        updater.setDescription("new black table");
        updater.setAvailable(false);

        ItemDto updated = itemService.updateItem(updater, item.getId(), owner.getId());

        Assertions.assertThat(updated).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updater);
    }

    @Test
    void getAllItems_Normal() {
        Long ownerId = owner.getId();
        Long bookerId = booker.getId();

        List<ItemDtoByOwner> returnedList = itemService.findAll(ownerId, 0, 2);
        Assertions.assertThat(returnedList)
                .isNotEmpty()
                .hasSize(2);
        Assertions.assertThat(returnedList.get(0).getName()).isEqualTo(item.getName());
        Assertions.assertThat(returnedList.get(1).getName()).isEqualTo(item2.getName());


        List<ItemDtoByOwner> returnedList2 = itemService.findAll(bookerId, 0, 1);
        Assertions.assertThat(returnedList2).isEmpty();
    }

    @Test
    void findItems_Normal() {
        String search = "black";

        List<ItemDto> list = itemService.findItemByDescription(search, 0, 2);

        Assertions.assertThat(list).isNotEmpty().hasSize(2);
        Assertions.assertThat(list.get(0).getName()).isEqualTo(item.getName());
        Assertions.assertThat(list.get(1).getName()).isEqualTo(item2.getName());
    }

    @Test
    void findItems_EmptySearchText_Normal() {
        String search = "";

        List<ItemDto> list = itemService.findItemByDescription(search, 0, 1);

        Assertions.assertThat(list).isEmpty();
    }

    @Test
    void addComment_Normal() {
        CommentDto commentNewDto = CommentDto.builder()
                .text("perfect table")
                .build();

        CommentDto addedComment = itemService.addComment(commentNewDto, booker.getId(), item.getId());

        Assertions.assertThat(addedComment).isNotNull()
                .hasFieldOrPropertyWithValue("text", commentNewDto.getText())
                .hasFieldOrPropertyWithValue("authorName", booker.getName());

    }
}
