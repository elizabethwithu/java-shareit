package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserDao {
    User createUser(User user);

    User findUserById(long id);

    User updateUser(User user, long id);

    void removeUserById(long id);

    Collection<User> findAll();
}
