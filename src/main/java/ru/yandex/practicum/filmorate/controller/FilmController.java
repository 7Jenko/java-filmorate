package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 1;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        try {
        validateDuration(film);
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Создан фильм с ID: {}", film.getId());
        return film;
        } catch (ValidationException exp) {
            log.error("Ошибка валидации при создании фильма: {}", exp.getMessage());
            throw exp;
        }
    }

    @PutMapping("/{id}")
    public Film updateFilm(@PathVariable int id, @Valid @RequestBody Film film) {
        if (!films.containsKey(id)) {
            log.error("Не найден фильм с ID: {}", id);
            throw new ValidationException("Не найден фильм с id " + id);
        }
        try {
        validateDuration(film);
        Film updatedFilm = films.get(id);
        updatedFilm.setName(film.getName());
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());
        log.info("Обновлен фильм с ID: {}", id);
        return updatedFilm;
        } catch (ValidationException exp) {
            log.error("Ошибка валидации при обновлении фильма: {}", exp.getMessage());
            throw exp;
        }
    }

    @GetMapping
    public Map<Integer, Film> getAllFilms() {
        return films;
    }

    private void validateDuration(Film film) {
        if (film.getDuration() == null || film.getDuration().toMillis() <= 0) {
            String errorMessage = "Продолжительность фильма должна быть положительным числом.";
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}