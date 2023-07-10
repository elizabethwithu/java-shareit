package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
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
