package ru.practicum.shareit.request;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class ItemRequest {
    private Long id;

    @NotBlank(message = "Описание запроса пустое.")
    private String description;

    @NotNull(message = "Пользователь запроса не указан.")
    private User requester;

    @NotNull(message = "Дата создания не указана.")
    private LocalDateTime created;
}
