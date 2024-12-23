package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeStorage likeStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    public void addLike(int filmId, int userId) {
        log.debug("Пользователь с ID {} ставит лайк фильму с ID {}", userId, filmId);

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        likeStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        log.debug("Пользователь с ID {} удаляет лайк у фильма с ID {}", userId, filmId);

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (!likeStorage.existsLike(filmId, userId)) {
            log.warn("Лайк от пользователя с ID {} для фильма с ID {} не найден", userId, filmId);
            throw new NotFoundException("Лайк от пользователя с ID " + userId + " для фильма с ID " + filmId + " не найден");
        }

        likeStorage.deleteLike(filmId, userId);
        log.info("Лайк от пользователя с ID {} для фильма с ID {} успешно удален", userId, filmId);
    }

    public List<Film> getPopular(Integer count) {
        log.debug("Запрос на получение популярных фильмов с count: {}", count);
        try {
            List<Film> popularFilms = likeStorage.getPopular(count);
            if (popularFilms == null) {
                popularFilms = new ArrayList<>();
            }
            return popularFilms;
        } catch (Exception e) {
            log.error("Ошибка при получении популярных фильмов", e);
            return new ArrayList<>();
        }
    }
}