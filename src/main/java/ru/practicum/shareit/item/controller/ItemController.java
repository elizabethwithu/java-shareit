package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemServiceImpl service;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto dto) {
        return service.createItem(dto, userId);
    }

    @PatchMapping("{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto dto,
                              @PathVariable Long itemId) {
        return service.updateItem(dto, itemId, userId);
    }

    @GetMapping("{itemId}")
    public ItemDtoByOwner findItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return service.findItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoByOwner> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.findAll(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemByDescription(@RequestParam(required = false) String text) {
        return service.findItemByDescription(text);
    }

    @DeleteMapping("{itemId}")
    public void removeItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long itemId) {
        service.removeItemById(userId, itemId);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody CommentDto commentDto,
                              @PathVariable Long itemId) {
        return service.addComment(commentDto, userId, itemId);
    }
}
