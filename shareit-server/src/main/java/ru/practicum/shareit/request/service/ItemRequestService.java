package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto);

    List<ItemRequestDtoByOwner> findAllUsersRequestsWithReplies(Long userId);

    List<ItemRequestDtoByOwner> findAll(Long userId, int from, int size);

    ItemRequestDtoByOwner findByIdWithReplies(Long userId, Long requestId);
}
