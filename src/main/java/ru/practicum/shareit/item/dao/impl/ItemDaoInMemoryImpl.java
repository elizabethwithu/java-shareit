package ru.practicum.shareit.item.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ItemDaoInMemoryImpl implements ItemDao {
    private final UserDao userDao;
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Long>> usersItems = new HashMap<>();
    private long id = 1;

    @Override
    public Item createItem(Item item, long userId) {
        User user = userDao.findUserById(userId);

        item.setId(id++);
        item.setOwner(user);

        items.put(item.getId(), item);
        usersItems.computeIfAbsent(userId, k -> new ArrayList<>()).add(item.getId());
        log.info("Добавлена вещь {}", item);
        return item;
    }

    @Override
    public Item updateItem(Item item, long itemId, long userId) {
        checkItemAvailability(itemId);
        checkAccess(userId, itemId);

        Item oldItem = items.get(itemId);
        if (item.getName() == null) {
            item.setName(oldItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(oldItem.getAvailable());
        }

        items.remove(itemId);
        item.setId(itemId);
        item.setOwner(oldItem.getOwner());
        items.put(itemId, item);
        log.info("Обновлена вещь {}", item);
        return item;
    }

    @Override
    public Item findItemById(long itemId) {
        checkItemAvailability(itemId);
        log.info("Найдена вещь с айди {}", itemId);
        return items.get(itemId);
    }

    @Override
    public Collection<Item> findAll(long userId) {
        List<Long> itemsId = usersItems.getOrDefault(userId, new ArrayList<>());
        log.info("Найден список вещей пользователя с айди {}", userId);
        return itemsId.stream()
                .map(items::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItemByDescription(String text) {
        List<Item> matchItems = items.values().stream()
                .filter(item -> item.getAvailable()
                        && item.getDescription().toLowerCase().contains(text)
                        || item.getName().toLowerCase().contains(text))
                .collect(Collectors.toList());
        log.info("Найден список вещей по текстовому запросу {}", text);
        return matchItems;
    }

    @Override
    public void removeItemById(long userId, long itemId) {
        checkAccess(userId, itemId);
        items.remove(itemId);
        log.info("Удален пользователь с айди {}", userId);
    }

    private void checkItemAvailability(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Вещь с указанным айди не найдена.");
        }
    }

    private void checkAccess(long userId, long itemId) {
        Item item = items.get(itemId);
        Long ownerId = item.getOwner().getId();
        if (!Objects.equals(userId, ownerId)) {
            throw new NotFoundException("Редактирование вещи доступно только владельцу.");
        }
    }
}
