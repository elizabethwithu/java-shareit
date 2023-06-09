package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto dto, long userId);

    ItemDto updateItem(ItemDto dto, long itemId, long userId);

    ItemDto findItemById(long itemId);

    List<ItemDto> findAll(long userId);

    List<ItemDto> findItemByDescription(String text);

    void removeItemById(long userId, long itemId);
}
