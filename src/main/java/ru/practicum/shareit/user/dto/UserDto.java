package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserDto {
    private Long id;

    @NotBlank(message = "Имя пользователя не указано.")
    private String name;

    @NotBlank(message = "Почтовый адрес пустой.")
    @Email(message = "Почтовый адрес не соответствует требованиям.")
    private String email;
}
