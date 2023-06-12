package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class Item {
    private Long id;

    @NotBlank(message = "Наименование элемента отсутствует.")
    private String name;

    @NotBlank(message = "Описание элемента пустое.")
    private String description;

    private Boolean available;

    private User owner;

    private ItemRequest request;
}
