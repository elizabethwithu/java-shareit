package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Override
    public UserDto createUser(UserDto dto) {
        User user = UserMapper.toUser(dto);
        User savedUser = userDao.save(user);
        log.info("Создан пользователь {}.", savedUser);
        return UserMapper.doUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserById(long id) {
        User user = userDao.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        log.info("Найден пользователь с айди {}.", id);
        return UserMapper.doUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto dto, long id) {
        User oldUser = userDao.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            oldUser.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            oldUser.setEmail(dto.getEmail());
        }

        User savedUser = userDao.save(oldUser);
        log.info("Пользователь с айди {} успешно обновлен.", id);
        return UserMapper.doUserDto(savedUser);
    }

    @Override
    public void removeUserById(long id) {
        userDao.deleteById(id);
        log.info("Пользователь с айди успешно удален {}.", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        List<UserDto> users = userDao.findAll().stream()
                .map(UserMapper::doUserDto)
                .collect(Collectors.toList());
        log.info("Всё пользователи успешно получены.");
        return users;
    }
}
