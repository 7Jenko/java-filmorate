package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;
    private final DirectorStorage directorStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper mapper, DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlQuery = "SELECT films.film_id, films.film_name, films.description, films.duration, "
                + "films.release_date, films.rating_id, rating_mpa.rating_name "
                + "FROM films "
                + "JOIN rating_mpa ON films.rating_id = rating_mpa.rating_id";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            String name = rs.getString("film_name");
            String description = rs.getString("description");
            Long duration = rs.getLong("duration");

            LocalDate releaseDate = rs.getTimestamp("release_date") != null
                    ? rs.getTimestamp("release_date").toLocalDateTime().toLocalDate()
                    : null;

            int mpaId = rs.getInt("rating_id");
            String mpaName = rs.getString("rating_name");
            RatingMpa mpa = new RatingMpa(mpaId, mpaName);

            Set<Genre> genres = getGenres(filmId);
            Set<Director> directors = directorStorage.getDirectorsByFilmId(filmId);

            return buildFilm(filmId, name, description, duration, releaseDate, mpa, genres, directors);
        });
    }

    @Override
    public Film createFilm(Film film) {
        Map<String, Object> keys = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("films")
                .usingColumns("film_name", "description", "duration", "release_date", "rating_id")
                .usingGeneratedKeyColumns("film_id")
                .executeAndReturnKeyHolder(Map.of("film_name", film.getName(),
                        "description", film.getDescription(),
                        "duration", film.getDuration(),
                        "release_date", java.sql.Date.valueOf(film.getReleaseDate()),
                        "rating_id", film.getMpa().getId()))
                .getKeys();
        film.setId((Integer) keys.get("film_id"));
        addGenre((Integer) keys.get("film_id"), film.getGenres());

        directorStorage.saveDirectors(film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        String sqlQuery = "UPDATE films "
                + "SET film_name = ?, "
                + "description = ?, "
                + "duration = ?, "
                + "release_date = ?, "
                + "rating_id = ? "
                + "WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getDuration(),
                film.getReleaseDate(), film.getMpa().getId(), film.getId());
        addGenre(film.getId(), film.getGenres());
        int filmId = film.getId();
        film.setGenres(getGenres(filmId));
        directorStorage.saveDirectors(film);
        film.setDirectors(directorStorage.getDirectorsByFilmId(filmId));

        return film;
    }

    @Override
    public void removeFilm(int filmId) {
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public Film getFilmById(int filmId) {
        String sqlQuery = "SELECT * FROM films "
                + "JOIN rating_mpa ON films.rating_id = rating_mpa.rating_id "
                + "WHERE film_id = ?";
        SqlRowSet srs = jdbcTemplate.queryForRowSet(sqlQuery, filmId);
        if (srs.next()) {
            return filmMap(srs);
        } else {
            throw new NotFoundException("Movie with ID = " + filmId + " not found");
        }
    }

    public void addGenre(int filmId, Set<Genre> genres) {
        deleteAllGenresById(filmId);
        if (genres == null || genres.isEmpty()) {
            return;
        }
        String sqlQuery = "INSERT INTO film_genres (film_id, genre_id) "
                + "VALUES (?, ?)";
        List<Genre> genresTable = new ArrayList<>(genres);
        this.jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, genresTable.get(i).getId());
            }

            public int getBatchSize() {
                return genresTable.size();
            }
        });
    }

    private Set<Genre> getGenres(int filmId) {
        Comparator<Genre> compId = Comparator.comparing(Genre::getId);
        Set<Genre> genres = new TreeSet<>(compId);
        String sqlQuery = "SELECT film_genres.genre_id, genres.genre_name FROM film_genres "
                + "JOIN genres ON genres.genre_id = film_genres.genre_id "
                + "WHERE film_id = ? ORDER BY genre_id ASC";
        genres.addAll(jdbcTemplate.query(sqlQuery, this::makeGenre, filmId));
        return genres;
    }

    private void deleteAllGenresById(int filmId) {
        String sglQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sglQuery, filmId);
    }

    public void addLike(int filmId, int userId) {
        String sqlQuery = "INSERT INTO likes (film_id, user_id) "
                + "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sqlQuery = "DELETE likes "
                + "WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        String sqlQuery = "SELECT films.film_id, films.film_name, films.description, films.duration, " +
                "films.release_date, films.rating_id, rating_mpa.rating_name " +
                "FROM films " +
                "LEFT JOIN likes ON likes.film_id = films.film_id " +
                "JOIN rating_mpa ON films.rating_id = rating_mpa.rating_id " +
                "WHERE " +
                "    (? IS NULL OR films.film_id IN ( " +
                "        SELECT film_genres.film_id " +
                "        FROM film_genres " +
                "        WHERE film_genres.genre_id = ? " +
                "    )) " +
                "AND " +
                "    (? IS NULL OR EXTRACT(YEAR FROM films.release_date) = ?) " +
                "GROUP BY films.film_id, films.film_name, films.description, films.duration, " +
                "films.release_date, films.rating_id, rating_mpa.rating_name " +
                "ORDER BY COUNT(likes.film_id) DESC " +
                "LIMIT ?;";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            String name = rs.getString("film_name");
            String description = rs.getString("description");
            Long duration = rs.getLong("duration");

            LocalDate releaseDate = rs.getTimestamp("release_date") != null
                    ? rs.getTimestamp("release_date").toLocalDateTime().toLocalDate()
                    : null;

            int mpaId = rs.getInt("rating_id");
            String mpaName = rs.getString("rating_name");
            RatingMpa mpa = new RatingMpa(mpaId, mpaName);

            Set<Genre> genres = getGenres(filmId);

            return buildFilm(filmId, name, description, duration, releaseDate, mpa, genres);
        }, genreId, genreId, year, year, count);
    }

    private List<Film> addGenreForList(List<Film> films) {
        Map<Integer, Film> filmsTable = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        String inSql = String.join(", ", Collections.nCopies(filmsTable.size(), "?"));
        final String sqlQuery = "SELECT * "
                + "FROM film_genres "
                + "LEFT OUTER JOIN genres ON film_genres.genre_id = genres.genre_id "
                + "WHERE film_genres.film_id IN (" + inSql + ") "
                + "ORDER BY film_genres.genre_id";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            Film film = filmsTable.get(rs.getInt("film_id"));
            if (film != null) {
                film.addGenre(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
            }
        }, filmsTable.keySet().toArray());
        return new ArrayList<>(filmsTable.values());
    }

    private Genre makeGenre(ResultSet rs, int id) throws SQLException {
        int genreId = rs.getInt("genre_id");
        String genreName = rs.getString("genre_name");
        return new Genre(genreId, genreName);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int filmId = rs.getInt("film_id");
        String name = rs.getString("film_name");
        String description = rs.getString("description");
        Long duration = rs.getLong("duration");

        LocalDate releaseDate = rs.getTimestamp("release_date") != null
                ? rs.getTimestamp("release_date").toLocalDateTime().toLocalDate()
                : null;

        int mpaId = rs.getInt("rating_id");
        String mpaName = rs.getString("rating_name");
        RatingMpa mpa = new RatingMpa(mpaId, mpaName);

        Set<Genre> genres = new HashSet<>();
        do {
            int genreId = rs.getInt("genre_id");
            String genreName = rs.getString("genre_name");
            if (genreId != 0 && genreName != null) {
                genres.add(new Genre(genreId, genreName));
            }
        } while (rs.next());

        return buildFilm(filmId, name, description, duration, releaseDate, mpa, genres);
    }

    private Film filmMap(SqlRowSet srs) {
        int filmId = srs.getInt("film_id");
        String name = srs.getString("film_name");
        String description = srs.getString("description");
        Long duration = srs.getLong("duration");

        LocalDate releaseDate = Objects.requireNonNull(srs.getTimestamp("release_date"))
                .toLocalDateTime().toLocalDate();

        int mpaId = srs.getInt("rating_id");
        String mpaName = srs.getString("rating_name");
        RatingMpa mpa = new RatingMpa(mpaId, mpaName);

        Set<Genre> genres = getGenres(filmId);

        return buildFilm(filmId, name, description, duration, releaseDate, mpa, genres);
    }

    private Film buildFilm(int filmId, String name, String description, Long duration,
                           LocalDate releaseDate, RatingMpa mpa, Set<Genre> genres) {
        return Film.builder()
                .id(filmId)
                .name(name)
                .description(description)
                .duration(duration)
                .releaseDate(releaseDate)
                .mpa(mpa)
                .genres(genres)
                .build();
    }

    private Film buildFilm(int filmId, String name, String description, Long duration,
                           LocalDate releaseDate, RatingMpa mpa, Set<Genre> genres, Set<Director> directors) {
        return Film.builder()
                .id(filmId)
                .name(name)
                .description(description)
                .duration(duration)
                .releaseDate(releaseDate)
                .mpa(mpa)
                .genres(genres)
                .directors(directors)
                .build();
    }

    public List<Film> getDirectorFilmSortedByLike(Long directorId) {
        String getDirectorFilmSortedByLikeQuery = "SELECT f.film_id, f.film_name, f.description, f.duration, \n" +
                "       f.release_date, f.rating_id, rm.rating_name, COUNT(l.user_id) AS like_count \n" +
                "FROM films f \n" +
                "JOIN film_directors fd ON f.film_id = fd.film_id \n" +
                "JOIN rating_mpa rm ON f.rating_id = rm.rating_id \n" +
                "LEFT JOIN likes l ON f.film_id = l.film_id \n" +
                "WHERE fd.director_id = ? \n" +
                "GROUP BY f.film_id, f.film_name, f.description, f.duration, f.release_date, f.rating_id, rm.rating_name \n" +
                "ORDER BY like_count DESC;\n";

        return jdbcTemplate.query(getDirectorFilmSortedByLikeQuery, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            String name = rs.getString("film_name");
            String description = rs.getString("description");
            Long duration = rs.getLong("duration");

            LocalDate releaseDate = rs.getTimestamp("release_date") != null
                    ? rs.getTimestamp("release_date").toLocalDateTime().toLocalDate()
                    : null;

            int mpaId = rs.getInt("rating_id");
            String mpaName = rs.getString("rating_name");
            RatingMpa mpa = new RatingMpa(mpaId, mpaName);

            Set<Genre> genres = getGenres(filmId);

            Set<Director> directors = directorStorage.getDirectorsByFilmId(filmId);

            return buildFilm(filmId, name, description, duration, releaseDate, mpa, genres, directors);
        }, directorId);
    }


    public List<Film> getDirectorFilmSortedByYear(Long directorId) {
        String getDirectorFilmSortedByYearQuery = "SELECT f.film_id, f.film_name, f.description, f.duration, " +
                "f.release_date, f.rating_id, rm.rating_name " +
                "FROM films f " +
                "JOIN rating_mpa rm ON f.rating_id = rm.rating_id " +
                "JOIN film_directors fd ON f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "ORDER BY EXTRACT(YEAR FROM f.release_date) ASC";

        return jdbcTemplate.query(getDirectorFilmSortedByYearQuery, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            String name = rs.getString("film_name");
            String description = rs.getString("description");
            Long duration = rs.getLong("duration");

            LocalDate releaseDate = rs.getTimestamp("release_date") != null
                    ? rs.getTimestamp("release_date").toLocalDateTime().toLocalDate()
                    : null;

            int mpaId = rs.getInt("rating_id");
            String mpaName = rs.getString("rating_name");
            RatingMpa mpa = new RatingMpa(mpaId, mpaName);

            Set<Genre> genres = getGenres(filmId);

            Set<Director> directors = directorStorage.getDirectorsByFilmId(filmId);

            return buildFilm(filmId, name, description, duration, releaseDate, mpa, genres, directors);
        }, directorId);
    }
}