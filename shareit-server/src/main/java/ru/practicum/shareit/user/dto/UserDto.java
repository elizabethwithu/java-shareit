package ru.practicum.shareit.user.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserDto {
    private Long id;

    private String name;

    private String email;
}
