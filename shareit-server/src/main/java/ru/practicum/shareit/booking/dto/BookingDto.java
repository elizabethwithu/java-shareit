package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private long id;

    @NotNull(message = "Элемент бронирования отсутствует.")
    private Long itemId;

    @NotNull(message = "Дата начала бронирования не указана.")
    @FutureOrPresent(message = "Дата начала бронирования указана в прошлом.")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования не указана.")
    @FutureOrPresent(message = "Дата окончания бронирования указана в прошлом.")
    private LocalDateTime end;

    private Long bookerId;

    private BookingStatus status;
}
