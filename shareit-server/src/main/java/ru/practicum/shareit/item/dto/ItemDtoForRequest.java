package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

@AllArgsConstructor
@Getter
@Setter
public class ItemDtoForRequest {
    private Long id;

    private String name;

    private User owner;
}
