package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private Long id;

    @NotBlank(message = "Имя пользователя не указано.")
    private String name;

    @NotNull(message = "Почтовый адрес пустой.")
    @Email(message = "Почтовый адрес не соответствует требованиям")
    private String email;
}
