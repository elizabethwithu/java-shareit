package ru.practicum.shareit.request.dto;

import lombok.*;
import org.apache.catalina.User;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание запроса пустое.")
    private String description;

    private User requester;

    private LocalDateTime created = LocalDateTime.now();
}
