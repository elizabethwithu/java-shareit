package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserDao repository;

    @InjectMocks
    private UserServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .name("nick")
                .email("nick@example.com")
                .build();
    }

    @Test
    void succeedCreateUser() {
        User userToSave = User.builder().name("nick").email("nick@example.com").build();
        when(repository.save(any())).thenReturn(user);

        UserDto userDto = UserMapper.doUserDto(userToSave);
        UserDto userSaved = service.createUser(userDto);

        assertNotNull(userSaved);
        assertEquals(user.getId(), userSaved.getId());
        verify(repository, times(1)).save(any());
    }

    @Test
    void createUserWithExistingEmail() {
        User userToSave = User.builder().name("nick").email("nick@example.com").build();
        UserDto userDto = UserMapper.doUserDto(userToSave);

        when(repository.save(any())).thenThrow(new RuntimeException("uq_user_email"));
        AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> service.createUser(userDto)
        );

        String error = String.format("%s уже зарегистрирован.", user.getEmail());

        assertEquals(error, exception.getMessage());
    }

    @Test
    void succeedUpdateUserName() {
        long userId = user.getId();
        User userUpdated = User.builder().id(userId).name("fred").email(user.getEmail()).build();

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(userUpdated);

        UserDto userDtoUpdated = service.updateUser(UserDto.builder().name("fred").build(), (userId));

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals("fred", userDtoUpdated.getName());
    }

    @Test
    void updateUserBlancName() {
        long userId = user.getId();
        User userUpdated = User.builder().id(userId).name("nick").email(user.getEmail()).build();

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(userUpdated);

        UserDto userDtoUpdated = service.updateUser(UserDto.builder().name("  ").build(), (userId));

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals("nick", userDtoUpdated.getName());
    }

    @Test
    void succeedUpdateUserEmail() {
        long userId = user.getId();
        User userUpdated = User.builder().id(userId).name(user.getName()).email("fred@mail.ru").build();
        when(repository.findById(userId)).thenReturn(Optional.of(userUpdated));
        when(repository.save(any())).thenReturn(userUpdated);
        User userToSave = User.builder().name("nick").email("nick@example.com").build();
        UserDto userDto = UserMapper.doUserDto(userToSave);
        service.createUser(userDto);

        UserDto userDtoUpdated = service.updateUser(UserDto.builder().email("fred@mail.ru").build(), userId);

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals("fred@mail.ru", userDtoUpdated.getEmail());
    }

    @Test
    void updateUserBlancEmail() {
        long userId = user.getId();
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(user);
        UserDto userDtoUpdated = service.updateUser(UserDto.builder().email("  ").build(), userId);

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals(user.getEmail(), userDtoUpdated.getEmail());
    }

    @Test
    void updateNotFoundUser() {
        long userIdNotFound = 0L;
        UserDto userUpdated = UserDto.builder().name("nick").email("nick@mail.ru").build();
        when(repository.findById(userIdNotFound)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.updateUser(userUpdated, userIdNotFound));

        assertEquals("Пользователь не найден.", exception.getMessage());
    }

    @Test
    void succeedFindUserById() {
        long userId = user.getId();
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        UserDto userFound = service.findUserById(userId);

        assertNotNull(userFound);
        assertEquals(userId, userFound.getId());
    }

    @Test
    void findUserByIdShouldThrowException() {
        long userIdNotFound = 0L;
        when(repository.findById(userIdNotFound)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findUserById(userIdNotFound));

        assertEquals("Пользователь не найден.", exception.getMessage());
    }

    @Test
    void succeedFindAll() {
        when(repository.findAll()).thenReturn(List.of(user));

        List<UserDto> users = service.findAll();

        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void succeedFindAllWithoutUsers() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> users = service.findAll();

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    void succeedRemoveUser() {
        long userId = 1L;

        service.removeUserById(userId);

        verify(repository, times(1)).deleteById(userId);
    }

    @Test
    void checkUserAvailability() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> UserServiceImpl.checkUserAvailability(repository, 5L));

        assertEquals("Пользователь с запрашиваемым айди не зарегистрирован.", exception.getMessage());
    }
}
