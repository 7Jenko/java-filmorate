package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikeDbStorage likeDbStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, LikeDbStorage likeDbStorage) {
        this.filmStorage = filmStorage;
        this.likeDbStorage = likeDbStorage;
    }

    public Film addFilm(Film film) {
        log.debug("Добавление фильма: {}", film);
        return filmStorage.createFilm(film);
    }

    public Collection<Film> getTopFilms(Integer count) {
        return filmStorage.getMostPopularFilms(count);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        return film;
    }

    public void removeFilm(int filmId) {
        if (filmStorage.getFilmById(filmId) == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        filmStorage.removeFilm(filmId);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        likeDbStorage.addLike(filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        likeDbStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} отменил лайк фильма {}", userId, filmId);
    }
}