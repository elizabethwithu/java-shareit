package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return new ItemRequest(
                dto.getId(),
                dto.getDescription(),
                dto.getRequester(),
                dto.getCreated()
        );
    }

    public static ItemRequestDto doItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequester(),
                request.getCreated()
        );
    }

    public static ItemRequestDtoByOwner doItemRequestDtoByOwner(ItemRequest request, List<ItemDto> reply) {
        return new ItemRequestDtoByOwner(
                request.getId(),
                request.getDescription(),
                request.getRequester(),
                request.getCreated(),
                reply
        );
    }
}
