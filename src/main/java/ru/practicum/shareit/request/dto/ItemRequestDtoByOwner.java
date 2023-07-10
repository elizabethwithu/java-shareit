package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDtoByOwner {
    private Long id;

    private String description;

    private User requester;

    private LocalDateTime created;

    private List<ItemDto> items;
}
