package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

@Data
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

    private Set<Integer> likes = new HashSet<>();

    public Film(int id, String name, String description, LocalDate releaseDate, Long duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = new HashSet<>();
    }

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года.")
    private boolean isRightReleaseDate() {
        return this.releaseDate.isAfter(MIN_DATE_RELEASE);
    }
}
