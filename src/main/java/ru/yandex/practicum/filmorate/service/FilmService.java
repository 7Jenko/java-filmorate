package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmDbStorage filmDbStorage;
    private final FilmStorage filmStorage;
    private final LikeDbStorage likeDbStorage;
    private final DirectorStorage directorStorage;
    private final GenreDbStorage genreStorage;
    public final FilmRowMapper mapper;
    private final EventService eventService;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, FilmDbStorage filmDbStorage,
                       LikeDbStorage likeDbStorage, DirectorStorage directorStorage, GenreDbStorage genreDbStorage,
                       GenreDbStorage genreStorage, FilmRowMapper mapper, JdbcTemplate jdbcTemplate,
                       EventService eventService) {
        this.filmDbStorage = filmDbStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.likeDbStorage = likeDbStorage;
        this.directorStorage = directorStorage;
        this.genreStorage = genreStorage;
        this.mapper = mapper;
        this.eventService = eventService;
    }

    public Film addFilm(Film film) {
        log.info("Добавление фильма: {}", film);
        return filmStorage.createFilm(film);
    }

    public Collection<Film> getTopFilms(Integer count, Integer genreId, Integer year) {
        log.info("Ищем список популярных фильмов");
        return filmStorage.getMostPopularFilms(count, genreId, year);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            log.warn("Возвращаемый фильм с ID {} не найден", film.getId());
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int filmId) {
        log.info("Возвращаем фильм с id {} ", filmId);
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    public void removeFilm(int filmId) {
        log.info("Удаляем фильм с id {} ", filmId);
        filmStorage.removeFilmGenres(filmId);
        filmStorage.removeLikesByFilmId(filmId);

        if (filmStorage.getFilmById(filmId).isEmpty()) {
            log.warn("Удаляемый фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        filmStorage.removeFilm(filmId);
    }

    public List<Film> getAllFilms() {
        log.info("Возвращаем все фильмы");
        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId);

        if (userStorage.getUserById(userId).isEmpty()) {
            log.warn("Пользователь с ID {} не найден", userId);
            return;
        }

        likeDbStorage.addLike(filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
        eventService.createEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with ID " + filmId + " not found"));

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        likeDbStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} отменил лайк фильма {}", userId, filmId);
        eventService.createEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }


    public List<Film> getFilmsByDirectorSorted(Long directorId, String sortBy) {
        List<Film> films;
        SortType sortType = SortType.fromString(sortBy);

        log.info("Возвращаем отсортированный список фильмов режиссера {}", directorId);

        switch (sortType) {
            case YEAR -> films = filmStorage.getDirectorFilmSortedByYear(directorId);
            case LIKES -> films = filmStorage.getDirectorFilmSortedByLike(directorId);
            default -> throw new IllegalArgumentException("Неверный параметр сортировки: " + sortType);
        }

        if (films.isEmpty()) {
            log.warn("Фильмы не найдены для режиссера с id {}", directorId);
            throw new NotFoundException("Фильмы не найдены для режиссера с id " + directorId);
        }

        return films;
    }

    public Collection<Film> getRecommendations(Integer userId) {
        return filmStorage.getUserRecommendations(userId);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        log.info("Возвращаем общие фильмы пользователя {} и пользователя {}", userId, friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    private void setGenresForFilms(List<Film> films) {
        List<Integer> filmIds = films
                .stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Integer, List<Genre>> filmGenres = genreStorage.getGenresByFilmIds(filmIds);

        Map<Long, List<Genre>> filmGenres = (Map<Long, List<Genre>>) genreStorage.getAllGenres();
        log.info("Добавляем жанры к фильмам");

        for (Film film : films) {
            List<Genre> genres = filmGenres.getOrDefault(film.getId(), new ArrayList<>());
            film.setGenres(genres);
        }
    }

    private void setDirectorsForFilms(List<Film> films) {
        if (films == null) {
            return;
        }

        log.info("Добавляем режиссеров к фильмам");
        Map<Long, Set<Director>> filmsDirectors = directorStorage.getAllFilmsDirectors();

        for (Film film : films) {
            Set<Director> directors = filmsDirectors.getOrDefault(film.getId(), new HashSet<>());

            film.setDirectors(directors.isEmpty() ? null : directors);
        }
    }

    private void setAdditionalFieldsForFilms(List<Film> films) {
        setGenresForFilms(films);
        setDirectorsForFilms(films);
    }

    public List<Film> searchFilms(String query, List<FilmSearchCriteria> by) {
        List<Film> films;

        if (by.contains(FilmSearchCriteria.title) && by.contains(FilmSearchCriteria.director)) {
            log.info("Ищем фильм по названию и режиссеру - {}", query);
            films = filmStorage.searchFilmsByDirectorTitle(query);
        } else if (by.contains(FilmSearchCriteria.director)) {
            log.info("Ищем фильм по режиссеру - {}", query);
            films = filmStorage.searchFilmsByDirector(query);
        } else {
            log.info("Ищем фильм по названию - {}", query);
            films = filmDbStorage.searchFilmsByTitle(query);
        }

        return films;
    }
}