package ru.yandex.practicum.filmorate.storage.Review;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateReviewDto {
    @NotNull
    private Long reviewId;

    @Size(max = 400, message = "Длина отзыва должна быть от 1 до 400 символов.")
    private String content;

    private Boolean isPositive;
    private Long userId;
    private Long filmId;
}