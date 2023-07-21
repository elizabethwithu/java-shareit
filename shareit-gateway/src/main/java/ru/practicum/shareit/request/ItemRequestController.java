package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.Request;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(Request.USER_ID) Long userId,
                                                @Valid @RequestBody ItemRequestDto requestDto) {
        return itemRequestClient.createRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllWithReplies(@RequestHeader(Request.USER_ID) Long userId) {
        return itemRequestClient.findAllWithReplies(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@RequestHeader(Request.USER_ID) Long userId,
                                               @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                               @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        return itemRequestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByIdWithReplies(@RequestHeader(Request.USER_ID) Long userId,
                                                     @PathVariable Long requestId) {
        return itemRequestClient.findByIdWithReplies(userId, requestId);
    }
}
