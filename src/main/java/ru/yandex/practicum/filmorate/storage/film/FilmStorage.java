package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    List<Film> getAllFilms();

    void deleteById(int filmId);

    List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year);

    List<Film> getDirectorFilmSortedByLike(Long directorId);

    List<Film> getDirectorFilmSortedByYear(Long directorId);
}