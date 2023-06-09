package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto dto);

    UserDto findUserById(long id);

    UserDto updateUser(UserDto dto, long id);

    void removeUserById(long id);

    List<UserDto> findAll();
}
