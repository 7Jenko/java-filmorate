package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void deleteAllGenresById(int filmId) {
        String sqlQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public Genre getGenreById(int genreId) {
        String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";
        SqlRowSet srs = jdbcTemplate.queryForRowSet(sqlQuery, genreId);
        if (srs.next()) {
            return new Genre(genreId, srs.getString("genre_name"));
        }
        throw new NotFoundException("Жанр не найден: id = " + genreId);
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();
        String sqlQuery = "SELECT * FROM genres ";
        SqlRowSet srs = jdbcTemplate.queryForRowSet(sqlQuery);
        while (srs.next()) {
            genres.add(new Genre(srs.getInt("genre_id"), srs.getString("genre_name")));
        }
        return genres;
    }

    @Override
    public Map<Integer, List<Genre>> getGenresByFilmIds(List<Integer> filmIds) {
        Map<Integer, List<Genre>> filmGenres = new HashMap<>();

        StringBuilder sqlBuilder = new StringBuilder("SELECT fg.film_id, g.genre_id, g.genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id IN (");
        for (int i = 0; i < filmIds.size(); i++) {
            sqlBuilder.append("?");
            if (i < filmIds.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");

        String sql = sqlBuilder.toString();

        List<Genre> genreList = jdbcTemplate.query(sql, (PreparedStatement ps) -> {
            for (int i = 0; i < filmIds.size(); i++) {
                ps.setInt(i + 1, filmIds.get(i));
            }
        }, new GenreRowMapper());

        for (Genre genre : genreList) {
            int filmId = genre.getId();
            filmGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        }

        return filmGenres;
    }
}