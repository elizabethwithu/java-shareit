package ru.practicum.shareit.item.service;

import ru.practicum.shareit.exception.NotAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Objects;

public interface ItemService {
    ItemDto createItem(ItemDto dto, Long userId);

    ItemDto updateItem(ItemDto dto, long itemId, long userId);

    ItemDtoByOwner findItemById(long userId, long itemId);

    List<ItemDtoByOwner> findAll(long userId, int from, int size);

    List<ItemDto> findItemByDescription(String text, int from, int size);

    void removeItemById(long userId, long itemId);

    CommentDto addComment(CommentDto commentDto, long userId, long itemId);

    static void checkItemAvailability(ItemDao itemDao, long itemId) {
        if (!itemDao.existsById(itemId)) {
            throw new NotFoundException("Вещь с указанным айди не найдена.");
        }
    }

    static void checkItemAccess(ItemDao itemDao, long userId, long itemId) {
        Item item = itemDao.getReferenceById(itemId);
        Long ownerId = item.getOwner().getId();
        if (!Objects.equals(userId, ownerId)) {
            throw new NotAccessException("Редактирование вещи доступно только владельцу.");
        }
    }
}
