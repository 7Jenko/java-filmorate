package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.Month;

@Data
@AllArgsConstructor
public class Film {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.of(1895, Month.DECEMBER, 28);
    private int id;

    @NotBlank(message = "Название не может быть пустым.")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
    private String description;

    @PastOrPresent(message = "Дата релиза не может быть раньше 28 декабря 1895 года.")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма должна быть положительным числом.")
    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private Long duration;

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года.")
    private boolean isRightReleaseDate() {
        return this.releaseDate.isAfter(MIN_DATE_RELEASE);
    }
}
