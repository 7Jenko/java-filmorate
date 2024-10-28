package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> filmLikes = new HashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        log.debug("Добавление фильма: {}", film);
        return filmStorage.createFilm(film);
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
        log.debug("Пользователь с ID {} ставит лайк фильму с ID {}", userId, filmId);
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!film.getLikes().contains(userId)) {
            film.getLikes().add(userId);
            filmStorage.updateFilm(film);
        } else {
            log.info("Пользователь с ID {} уже ставил лайк фильму с ID {}", userId, filmId);
        }
        log.info("Лайк добавлен: Пользователь {} -> Фильм {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Set<Integer> likes = filmLikes.get(filmId);
        if (likes != null) {
            likes.remove(userId);
        }
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> {
                    int f1Likes = filmLikes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int f2Likes = filmLikes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(f2Likes, f1Likes);
                })
                .limit(count)
                .collect(Collectors.toList());
    }
}