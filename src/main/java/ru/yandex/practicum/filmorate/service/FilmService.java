package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmDbStorage filmDbStorage;
    private final FilmStorage filmStorage;
    private final LikeDbStorage likeDbStorage;
    private final DirectorStorage directorStorage;
    private final GenreDbStorage genreStorage;
    public final FilmRowMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, FilmDbStorage filmDbStorage, LikeDbStorage likeDbStorage, DirectorStorage directorStorage, GenreDbStorage genreDbStorage, GenreDbStorage genreStorage, FilmRowMapper mapper, JdbcTemplate jdbcTemplate) {
        this.filmDbStorage = filmDbStorage;
        this.filmStorage = filmStorage;
        this.likeDbStorage = likeDbStorage;
        this.directorStorage = directorStorage;
        this.genreStorage = genreStorage;
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
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

    public List<Film> getFilmsByDirectorSorted(Long directorId, String sortBy) {
        if ("year".equalsIgnoreCase(sortBy)) {
            return getDirectorFilmSortedByYear(directorId);
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            return getDirectorFilmSortedByLike(directorId);
        } else {
            throw new IllegalArgumentException("Invalid sortBy parameter");
        }
    }

    private void setGenresForFilms(List<Film> films) {
        Map<Long, Set<Genre>> filmGenres = (Map<Long, Set<Genre>>) genreStorage.getAllGenres();
        for (Film film : films) {
            Set<Genre> genres = filmGenres.getOrDefault(film.getId(), new HashSet<>());
            film.setGenres(genres);
        }
    }

    private void setDirectorsForFilms(List<Film> films) {
        if (films == null) {
            return;
        }
        Map<Long, Set<Director>> filmsDirectors = directorStorage.getAllFilmsDirectors();

        for (Film film : films) {
            Set<Director> directors = filmsDirectors.get(film.getId());

            if (directors == null || directors.isEmpty()) {
                film.setDirectors(null);
            } else {
                film.setDirectors(directors);
            }
        }
    }

    private void setAdditionalFieldsForFilms(List<Film> films) {
        setGenresForFilms(films);
        setDirectorsForFilms(films);
    }

    private List<Film> getDirectorFilmSortedByLike(Long directorId) {
        String getDirectorFilmSortedByLikeQuery = "SELECT f.*, fl.likes_count, mr.id AS mpa_id, mr.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN ( " +
                "    SELECT film_id, COUNT(user_id) AS likes_count " +
                "    FROM film_likes " +
                "    GROUP BY film_id " +
                ") fl ON fl.film_id = f.id " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_id = mr.id " +
                "WHERE f.id IN ( " +
                "    SELECT film_id " +
                "    FROM film_directors fd " +
                "    WHERE fd.director_id = ? " +
                ") " +
                "ORDER BY fl.likes_count DESC";

        return jdbcTemplate.query(getDirectorFilmSortedByLikeQuery, mapper, directorId);
    }

    private List<Film> getDirectorFilmSortedByYear(Long directorId) {
        String getDirectorFilmSortedByYearQuery = "SELECT f.*, " +
                "EXTRACT(YEAR FROM CAST(f.RELEASE_DATE AS DATE)) AS release_year, " +
                "mr.ID AS mpa_id, mr.name AS mpa_name " +
                "FROM FILMS f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_id = mr.id " +
                "WHERE f.ID IN ( " +
                "    SELECT film_id " +
                "    FROM FILM_DIRECTORS fd " +
                "    WHERE fd.director_id = ? " +
                ") " +
                "ORDER BY release_year ASC";

        return jdbcTemplate.query(getDirectorFilmSortedByYearQuery, mapper, directorId);
    }
}