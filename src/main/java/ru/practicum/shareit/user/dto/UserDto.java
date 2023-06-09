package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    long id;

    @NotBlank(message = "Имя пользователя не указано.")
    private String name;

    @NotNull(message = "Почтовый адрес пустой.")
    @Email(message = "Почтовый адрес не соответствует требованиям")
    private String email;
}
