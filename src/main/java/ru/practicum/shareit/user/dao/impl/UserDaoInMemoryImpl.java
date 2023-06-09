package ru.practicum.shareit.user.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class UserDaoInMemoryImpl implements UserDao {
    private final Map<Long, User> users = new HashMap<>();
    long id = 1;

    @Override
    public User createUser(User user) {
        checkUserUniqueness(user);

        user.setId(id++);
        users.put(user.getId(), user);
        log.info("Создан пользователь {}", user);
        return user;
    }

    @Override
    public User findUserById(long id) {
        checkUserAvailability(id);
        log.info("Найден пользователь с айди {}", id);
        return users.get(id);
    }

    @Override
    public User updateUser(User user, long id) {
        checkUserAvailability(id);
        User oldUser = users.get(id);

        if (!Objects.equals(user.getEmail(), oldUser.getEmail())) {
            checkUserUniqueness(user);
        }
        if (user.getName() == null) {
            user.setName(oldUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }

        users.remove(id);
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь с айди успешно обновлен {}", id);
        return user;
    }

    @Override
    public void removeUserById(long id) {
        checkUserAvailability(id);
        users.remove(id);
        log.info("Пользователь с айди успешно удален {}", id);
    }

    @Override
    public Collection<User> findAll() {
        log.info("Всё пользователь успешно получены");
        return users.values();
    }

    private void checkUserUniqueness(User user) {
        String email = user.getEmail();

        List<String> emails = users.values().stream().map(User::getEmail).collect(Collectors.toList());
        if (emails.contains(email)) {
            throw new AlreadyExistException(user);
        }
    }

    private void checkUserAvailability(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с запрашиваемым айди не зарегистрирован.");
        }
    }
}
