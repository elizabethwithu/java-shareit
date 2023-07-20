package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotValidParameterException;
import ru.practicum.shareit.item.dao.CommentDao;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ItemRequestDao itemRequestDao;

    @Override
    public ItemDto createItem(ItemDto dto, Long userId) {
        User user = userDao.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        Item item = ItemMapper.toItem(dto, doRequests(dto));
        item.setOwner(user);

        Item savedItem = itemDao.save(item);
        log.info("Добавлена вещь {}", savedItem);
        return ItemMapper.doItemDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto dto, long itemId, long userId) {
        User user = userDao.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        Item oldItem = itemDao.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена."));

        Item item = ItemMapper.toItem(dto, doRequests(dto));
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
        item.setOwner(user);

        Item newItem = itemDao.save(item);
        log.info("Обновлена вещь {}", newItem);
        return ItemMapper.doItemDto(newItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoByOwner findItemById(long userId, long itemId) {
        Item item = itemDao.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена."));
        List<Comment> comments = commentDao.findByItemId(itemId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> lastBookings = bookingDao.findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(itemId, userId,
                now, BookingStatus.REJECTED);
        List<Booking> nextBookings = bookingDao.findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(itemId, userId,
                now, BookingStatus.REJECTED);

        log.info("Найдена вещь с айди {}", itemId);
        return ItemMapper.doItemDtoByOwner(item, lastBookings, nextBookings, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoByOwner> findAll(long userId, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);

        List<Item> userItems = itemDao.findItemsByOwnerId(userId, page);
        List<Comment> comments = commentDao.findByItemIdIn(userItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList()));
        LocalDateTime now = LocalDateTime.now();

        log.info("Найден список вещей пользователя с айди {}", userId);
        return userItems.stream()
                .sorted(Comparator.comparing(Item::getId))
                .map(item -> ItemMapper.doItemDtoByOwner(item,
                        bookingDao.findByItemIdAndItemOwnerIdAndStartIsBeforeAndStatusIsNot(item.getId(), userId, now,
                                BookingStatus.REJECTED, page),
                        bookingDao.findByItemIdAndItemOwnerIdAndStartIsAfterAndStatusIsNot(item.getId(), userId, now,
                                BookingStatus.REJECTED, page),
                        comments))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findItemByDescription(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        PageRequest page = PageRequest.of(from / size, size);

        log.info("Найден список вещей по текстовому запросу {}", text);
        return itemDao.findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text, page)
                .stream()
                .map(ItemMapper::doItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(CommentDto commentDto, long userId, long itemId) {
        User user = userDao.findById(userId).orElseThrow(() -> new NotValidParameterException("Пользователь не найден."));
        Item item = itemDao.findById(itemId).orElseThrow(() -> new NotValidParameterException("Вещь не найдена."));

        Booking booking = bookingDao
                .findTopByStatusNotLikeAndBookerIdAndItemIdOrderByEndAsc(BookingStatus.REJECTED, userId, itemId);
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
        itemDao.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с не найдена."));
        checkItemAccess(itemDao, userId, itemId);
        itemDao.deleteById(itemId);
        log.info("Удалена вещь с айди {}", itemId);
    }

    private ItemRequest doRequests(ItemDto dto) {
        ItemRequest requests;
        if (dto.getRequestId() != null) {
            requests = itemRequestDao.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден."));
        } else {
            requests = null;
        }
        return requests;
    }
}
