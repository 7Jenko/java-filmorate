package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_LIKE_QUERY = "INSERT INTO LIKES VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM LIKES WHERE film_id = ? and user_id = ?";
    private static final String GET_MOST_POPULAR_LIKE_QUERY = "select f.*, mr.name mpa_name from films f " +
            "left join likes fl on f.id = fl.film_id join mpa mr on f.mpa_id = mr.id " +
            "group by f.name, f.id, mr.name order by count(fl.film_id) desc limit ?";



    @Override
    public void addLike(int filmId, int userId) {
        if (existsLike(filmId, userId)) {
            log.warn("Пользователь с ID {} уже поставил лайк фильму {}", userId, filmId);
            return;
        }
        jdbcTemplate.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        jdbcTemplate.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopular(Integer count) {
        return jdbcTemplate.query(GET_MOST_POPULAR_LIKE_QUERY, new FilmRowMapper(), count);
    }

    public boolean existsLike(int filmId, int userId) {
        String sqlQuery = "SELECT COUNT(*) AS count FROM likes WHERE film_id = ? AND user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, filmId, userId);

        if (rowSet.next()) {
            return rowSet.getInt("count") > 0;
        }

        return false;
    }
}