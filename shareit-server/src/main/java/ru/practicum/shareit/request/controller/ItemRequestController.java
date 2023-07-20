package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestBody ItemRequestDto requestDto) {
        return requestService.createRequest(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestDtoByOwner> findAllWithReplies(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.findAllUsersRequestsWithReplies(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoByOwner> findAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam int from,
                                               @RequestParam int size) {
        return requestService.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoByOwner findByIdWithReplies(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long requestId) {
        return requestService.findByIdWithReplies(userId, requestId);
    }
}
