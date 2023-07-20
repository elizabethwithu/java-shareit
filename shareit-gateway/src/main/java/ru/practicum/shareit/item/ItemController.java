package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/items")
@Validated
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody ItemDto dto) {
        return itemClient.createItem(userId, dto);
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto dto,
                              @PathVariable Long itemId) {
        return itemClient.updateItem(userId, dto, itemId);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object> findItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PathVariable Long itemId) {
        return itemClient.findItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                        @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        return itemClient.findAll(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItemByDescription(@RequestParam(required = false) String text,
                                               @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                               @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        return itemClient.findItemByDescription(text, from, size);
    }

    @DeleteMapping("{itemId}")
    public void removeItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long itemId) {
        itemClient.removeItemById(userId, itemId);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody CommentDto commentDto,
                                             @PathVariable Long itemId) {
        return itemClient.addComment(userId, commentDto, itemId);
    }
}
