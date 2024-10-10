package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {
    private int id;

    @NotBlank(message = "Название не может быть пустым.")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
    private String description;

    @PastOrPresent(message = "Дата релиза не может быть раньше 28 декабря 1895 года.")
    private LocalDate releaseDate;

    private Duration duration;
}
