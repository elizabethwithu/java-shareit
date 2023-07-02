package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    private Long id;

    @NotBlank(message = "Имя пользователя не указано.")
    private String name;

    @NotBlank(message = "Почтовый адрес пустой.")
    @Email(message = "Почтовый адрес не соответствует требованиям")
    private String email;
}
