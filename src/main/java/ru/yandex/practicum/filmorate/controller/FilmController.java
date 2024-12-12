package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.LikeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private final LikeService likeService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.debug("Запрос на создание фильма: {}", film);
        Film createdFilm = filmService.addFilm(film);
        log.info("Фильм создан: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable int filmId) {
        return filmService.getFilmById(filmId);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilm(@PathVariable int filmId) {
        filmService.removeFilm(filmId);
        log.info("Фильм с ID {} был удален", filmId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> getMostPopularFilms(
            @RequestParam(name = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(name = "genreId", required = false) Integer genreId,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        return filmService.getTopFilms(count, genreId, year);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam String sortBy) {
        return filmService.getFilmsByDirectorSorted(directorId, sortBy);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> searchFilms(@RequestParam String query, @RequestParam ArrayList<String> by) {
        return filmService.searchFilms(query, by);
    }
}