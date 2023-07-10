package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BookingOutputDto {
    private Long id;

    private ItemDto item;

    private LocalDateTime start;

    private LocalDateTime end;

    private UserDto booker;

    private BookingStatus status;
}
