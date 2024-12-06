package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    List<Film> getAllFilms();

    void deleteById(int filmId);

    List<Film> getMostPopularFilms(int count);
}