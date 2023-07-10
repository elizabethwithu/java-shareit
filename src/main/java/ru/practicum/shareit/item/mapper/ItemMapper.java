package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto doItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ?
                        item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto dto, ItemRequest requests) {
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setRequest(requests);
        return item;
    }

    public static ItemDtoByOwner doItemDtoByOwner(Item item, List<Booking> lastBookings, List<Booking> nextBookings,
                                                  List<Comment> comments) {
        List<CommentDto> commentDto = comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());

        Booking nextBooking = nextBookings.stream()
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        Booking lastBooking = lastBookings.stream()
                .max(Comparator.comparing(Booking::getStart)).orElse(null);

        return new ItemDtoByOwner(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ?
                        item.getRequest().getId() : null,
                lastBooking != null ? BookingMapper.doBookingDto(lastBooking) : null,
                nextBooking != null ? BookingMapper.doBookingDto(nextBooking) : null,
                commentDto
        );
    }

    public static ItemDtoForRequest doItemDtoForRequest(Item item, User owner) {
        return new ItemDtoForRequest(
                item.getId(),
                item.getName(),
                owner
        );
    }
}
