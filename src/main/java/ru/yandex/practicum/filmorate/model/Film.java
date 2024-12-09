package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.of(1895, Month.DECEMBER, 28);
    private int id;

    @NotBlank(message = "Название не может быть пустым.")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
    private String description;

    @NotNull(message = "Дата не может быть пустой")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма должна быть положительным числом.")
    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private Long duration;

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года.")
    private boolean isRightReleaseDate() {
        return releaseDate != null && !releaseDate.isBefore(MIN_DATE_RELEASE);
    }

    @NotNull(message = "Жанры не могут быть пустыми.")
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @NotNull(message = "Рейтинг MPA не может быть пустым.")
    private RatingMpa mpa;

    @NotNull(message = "Режиссеры не могут быть пустыми.")
    @Builder.Default
    private Set<Director> directors = new HashSet<>();

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void removeAllGenres() {
        genres.clear();
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }
}