package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTests {

    private FilmController filmController;
    private Film validFilm;
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        FilmStorage filmStorage = new InMemoryFilmStorage();
        FilmService filmService = new FilmService(filmStorage);
        filmController = new FilmController(filmService);

        validFilm = new Film(0, "Valid Film", "This is a valid film description.",
                LocalDate.of(2024, 10, 10), 120L);
    }

    @Test
    public void filmWithValidFilm() {
        Film createdFilm = filmController.createFilm(validFilm);
        assert createdFilm.getId() > 0;
    }

    @Test
    public void filmWithEmptyName() {
        Film filmWithEmptyName = new Film(0, "", "Description",
                LocalDate.of(2024, 10, 10), 120L);
        assertFalse(validator.validate(filmWithEmptyName).isEmpty(), "Ожидалась ошибка: Название " +
                "не может быть пустым.");
    }

    @Test
    public void filmWithLongDescription() {
        String longDescription = "a".repeat(201);
        Film filmWithLongDescription = new Film(0, "Film Name", longDescription,
                LocalDate.of(2024, 10, 10), 120L);
        assertFalse(validator.validate(filmWithLongDescription).isEmpty(), "Ожидалась ошибка: " +
                "Максимальная длина описания — 200 символов.");
    }

    @Test
    public void filmWithInvalidReleaseDate() {
        Film filmWithFutureReleaseDate = new Film(0, "Film Name", "Description",
                LocalDate.of(3000, 1, 1), 120L);
        assertFalse(validator.validate(filmWithFutureReleaseDate).isEmpty(), "Ожидалась ошибка: Дата релиза " +
                "не может быть раньше 28 декабря 1895 года.");
    }

    @Test
    public void filmWithNegativeDuration() {
        Film filmWithNegativeDuration = new Film(0, "Film Name", "Description",
                LocalDate.of(2024, 10, 10), (long) -120);
        assertFalse(validator.validate(filmWithNegativeDuration).isEmpty(),"Продолжительность фильма " +
                "должна быть положительным числом.");
    }

    @Test
    public void filmWithNullDuration() {
        Film filmWithNullDuration = new Film(0, "Film Name", "Description",
                LocalDate.of(2024, 10, 10), null);
        assertFalse(validator.validate(filmWithNullDuration).isEmpty(),"Продолжительность фильма " +
                "должна быть положительным числом.");
    }

    @Test
    public void filmWithReleaseDateIsBefore1895() {
        Film filmWithInvalidReleaseDate = new Film(0, "Film Name", "Description",
                LocalDate.of(1895, Month.DECEMBER, 27), 120L);
        assertFalse(validator.validate(filmWithInvalidReleaseDate).isEmpty(), "Ожидалась ошибка: " +
                "Дата релиза не может быть раньше 28 декабря 1895 года.");
    }
}