package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId;

    @NotBlank(message = "Отзыв не может быть пустым.")
    @Size(min = 1, max = 400, message = "Длина отзыва должна быть от 1 до 400 символов.")
    private String content;

    @NotNull(message = "Поле isPositive не может быть пустым.")
    private Boolean isPositive;

    @NotNull(message = "ID пользователя не может быть пустым.")
    private Long userId;

    @NotNull(message = "ID фильма не может быть пустым.")
    private Long filmId;

    private Long useful = 0L;
}