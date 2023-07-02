package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Mapper
public class BookingMapper {

    public static Booking toBooking(BookingDto dto, Item item, User booker) {
        return new Booking(
                dto.getId(),
                item,
                dto.getStart(),
                dto.getEnd(),
                booker,
                dto.getStatus()
        );
    }

    public static BookingDto doBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItem().getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getBooker().getId(),
                booking.getStatus()
        );
    }

    public static BookingOutputDto doBookingOutputDto(Booking booking) {
        ItemDto itemDto = ItemMapper.doItemDto(booking.getItem());
        UserDto userDto = UserMapper.doUserDto(booking.getBooker());
        return new BookingOutputDto(
                booking.getId(),
                itemDto,
                booking.getStart(),
                booking.getEnd(),
                userDto,
                booking.getStatus()
        );
    }

    public static List<BookingOutputDto> makeBookingsOutputList(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::doBookingOutputDto).collect(Collectors.toList());
    }
}
