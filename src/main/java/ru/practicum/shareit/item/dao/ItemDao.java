package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemDao {
    Item createItem(Item item, long userId);

    Item updateItem(Item item, long itemId, long userId);

    Item findItemById(long itemId);

    Collection<Item> findAll(long userId);

    List<Item> findItemByDescription(String text);

    void removeItemById(long userId, long itemId);
}
