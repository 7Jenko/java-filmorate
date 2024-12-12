package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    List<Film> getAllFilms();

    void removeFilm(int id);

    List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year);

    Collection<Film> getUserRecommendations(Integer userId);

    List<Film> getDirectorFilmSortedByLike(Long directorId);

    List<Film> getDirectorFilmSortedByYear(Long directorId);
}