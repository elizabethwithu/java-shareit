package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserServiceImpl userService;

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto dto) {
        return userService.createUser(dto);
    }

    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable long id) {
        return userService.findUserById(id);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@RequestBody UserDto dto, @PathVariable long id) {
        return userService.updateUser(dto, id);
    }

    @DeleteMapping("/{id}")
    public void removeUserById(@PathVariable long id) {
        userService.removeUserById(id);
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }
}
