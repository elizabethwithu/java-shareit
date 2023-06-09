package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotValidParameterException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserDto createUser(UserDto dto) {
        User user = UserMapper.toUser(dto);

        return UserMapper.doUserDto(userDao.createUser(user));
    }

    public UserDto findUserById(long id) {
        User user = userDao.findUserById(id);

        return UserMapper.doUserDto(user);
    }

    public UserDto updateUser(UserDto dto, long id) {
        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new NotValidParameterException("Имя не может быть пустым");
        }
        if (dto.getEmail() != null && dto.getEmail().isBlank()) {
            throw new NotValidParameterException("Почтовый адрес не может быть пустым");
        }

        User user = UserMapper.toUser(dto);
        return UserMapper.doUserDto(userDao.updateUser(user, id));
    }

    public void removeUserById(long id) {
        userDao.removeUserById(id);
    }

    public List<UserDto> findAll() {
        return userDao.findAll().stream().map(UserMapper::doUserDto).collect(Collectors.toList());
    }
}
