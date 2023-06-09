package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class ItemDto {
    private Long id;

    @NotBlank(message = "Наименование элемента отсутствует.")
    private String name;

    @NotBlank(message = "Описание элемента пустое.")
    private String description;

    @NotNull(message = "Доступность вещи не указана.")
    private Boolean available;

    private Long requestId;
}
