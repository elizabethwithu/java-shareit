package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotValidParameterException;
import ru.practicum.shareit.item.dao.CommentDao;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.service.UserService.checkUserAvailability;
import static ru.practicum.shareit.item.service.ItemService.checkItemAvailability;
import static ru.practicum.shareit.item.service.ItemService.checkItemAccess;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserDao userDao;
    private final BookingDao bookingDao;
    private final CommentDao commentDao;

    @Override
    public ItemDto createItem(ItemDto dto, Long userId) {
        checkUserAvailability(userDao, userId);
        Item item = ItemMapper.toItem(dto);
            item.setOwner(userDao.findById(userId).orElseThrow());

            Item savedItem = itemDao.save(item);
            log.info("Добавлена вещь {}", savedItem);
            return ItemMapper.doItemDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto dto, long itemId, long userId) {
        checkItemAvailability(itemDao, itemId);
        checkUserAvailability(userDao, userId);
        checkItemAccess(itemDao, userId, itemId);

        Item oldItem = itemDao.getReferenceById(itemId);
        Item item = ItemMapper.toItem(dto);
        if (item.getName() == null) {
            item.setName(oldItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(oldItem.getAvailable());
        }

        item.setId(itemId);
        item.setOwner(userDao.getReferenceById(userId));

        Item newItem = itemDao.save(item);
        log.info("Обновлена вещь {}", newItem);
        return ItemMapper.doItemDto(newItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoByOwner findItemById(long userId, long itemId) {
        checkItemAvailability(itemDao, itemId);
        checkUserAvailability(userDao, userId);
        User user = userDao.getReferenceById(userId);
        List<Booking> bookings = bookingDao.findByItemId(itemId);
        List<Comment> comments = commentDao.findByItemId(itemId);
        log.info("Найдена вещь с айди {}", itemId);
        return ItemMapper.doItemDtoByOwner(itemDao.getReferenceById(itemId), user, bookings, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoByOwner> findAll(long userId) {
        User user = userDao.getReferenceById(userId);
        List<Item> userItems = itemDao.findItemsByOwnerId(userId);
        List<Booking> bookings = bookingDao.findByItemOwnerIdOrderByStartDesc(userId);
        List<Comment> comments = commentDao.findByItemIdIn(userItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList()));
        log.info("Найден список вещей пользователя с айди {}", userId);
        return userItems.stream()
                .map(item -> ItemMapper.doItemDtoByOwner(item, user, bookings, comments))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findItemByDescription(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        log.info("Найден список вещей по текстовому запросу {}", text);
        return itemDao.findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text)
                .stream()
                .map(ItemMapper::doItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(CommentDto commentDto, long userId, long itemId) {
        checkUserAvailability(userDao, userId);
        checkItemAvailability(itemDao, itemId);
        Booking booking = bookingDao
                .findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(BookingStatus.REJECTED, userId, itemId);
        User user = userDao.getReferenceById(userId);
        Item item = itemDao.getReferenceById(itemId);
        Comment comment = CommentMapper.toComment(commentDto, user, item);

        if (booking == null) {
            throw new NotValidParameterException(String
                    .format("Пользователь %s не пользовался вещью %s.", user.getName(), item.getName()));
        }
        if (comment.getCreated().isBefore(booking.getEnd())) {
            throw new NotValidParameterException("Необходимо завершить аренду вещи для написания комментария.");
        }
        return CommentMapper.toCommentDto(commentDao.save(comment));
    }

    @Override
    public void removeItemById(long userId, long itemId) {
        checkItemAvailability(itemDao, itemId);
        checkItemAccess(itemDao, userId, itemId);
        itemDao.deleteById(itemId);
        log.info("Удален пользователь с айди {}", userId);
    }
}
