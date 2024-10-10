package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTests {

    private FilmController filmController;
    private Film validFilm;
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setValidFilm() {
        filmController = new FilmController();
        validFilm = new Film(0, "Valid Film", "This is a valid film description.",
                LocalDate.of(2024, 10, 10), Duration.ofMinutes(120));
    }

    @Test
    public void filmWithValidFilm() {
        Film createdFilm = filmController.createFilm(validFilm);
        assert createdFilm.getId() > 0;
    }

    @Test
    public void filmWithEmptyName() {
        Film filmWithEmptyName = new Film(0, "", "Description",
                LocalDate.of(2024, 10, 10), Duration.ofMinutes(120));
        assertFalse(validator.validate(filmWithEmptyName).isEmpty(), "Ожидалась ошибка: Название " +
                "не может быть пустым.");
    }

    @Test
    public void filmWithLongDescription() {
        String longDescription = "a".repeat(201);
        Film filmWithLongDescription = new Film(0, "Film Name", longDescription,
                LocalDate.of(2024, 10, 10), Duration.ofMinutes(120));
        assertFalse(validator.validate(filmWithLongDescription).isEmpty(), "Ожидалась ошибка: " +
                "Максимальная длина описания — 200 символов.");
    }

    @Test
    public void filmWithInvalidReleaseDate() {
        Film filmWithFutureReleaseDate = new Film(0, "Film Name", "Description",
                LocalDate.of(3000, 1, 1), Duration.ofMinutes(120));
        assertFalse(validator.validate(filmWithFutureReleaseDate).isEmpty(), "Ожидалась ошибка: Дата релиза " +
                "не может быть раньше 28 декабря 1895 года.");
    }

    @Test
    public void filmWithNegativeDuration() {
        Film filmWithNegativeDuration = new Film(0, "Film Name", "Description",
                LocalDate.of(2024, 10, 10), Duration.ofMinutes(-120));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithNegativeDuration));
        assert exception.getMessage().equals("Продолжительность фильма должна быть положительным числом.");
    }

    @Test
    public void filmWithNullDuration() {
        Film filmWithNullDuration = new Film(0, "Film Name", "Description",
                LocalDate.of(2024, 10, 10), null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithNullDuration));
        assert exception.getMessage().equals("Продолжительность фильма должна быть положительным числом.");
    }
}