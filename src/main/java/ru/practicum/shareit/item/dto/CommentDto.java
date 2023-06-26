package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class CommentDto {
    private Long id;

    @NotBlank(message = "Текст комментария отсутствует.")
    private String text;

    private String authorName;

    private Long itemId;

    private LocalDateTime created;
}
