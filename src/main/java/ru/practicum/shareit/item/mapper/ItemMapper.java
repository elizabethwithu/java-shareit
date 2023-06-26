package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public class ItemMapper {
    public static ItemDto doItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto dto) {
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(dto.getRequestId() != null ? dto.getRequestId() : null);
        item.setRequest(itemRequest);
        return item;
    }

    public static ItemDtoByOwner doItemDtoByOwner(Item item, User user, List<Booking> bookings, List<Comment> comments) {
        LocalDateTime now = LocalDateTime.now();
        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .filter(booking -> booking.getItem().getOwner().getId().equals(user.getId()))
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED)
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(now))
                .filter(booking -> booking.getItem().getOwner().getId().equals(user.getId()))
                .filter(booking -> booking.getStatus() != (BookingStatus.REJECTED))
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .max(Comparator.comparing(Booking::getStart)).orElse(null);

        List<CommentDto> commentDto = comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());

        return new ItemDtoByOwner(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                lastBooking != null ? BookingMapper.doBookingDto(lastBooking) : null,
                nextBooking != null ? BookingMapper.doBookingDto(nextBooking) : null,
                commentDto
        );
    }
}
