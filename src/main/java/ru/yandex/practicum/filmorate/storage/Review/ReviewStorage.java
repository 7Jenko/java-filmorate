package ru.yandex.practicum.filmorate.storage.Review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewStorage {
    private static final String FIND_ALL_QUERY = "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, " +
            "COALESCE(SUM(CASE WHEN rr.is_like = TRUE THEN 1 WHEN rr.is_like = FALSE THEN -1 ELSE 0 END), 0) AS useful " +
            "FROM reviews AS r " +
            "LEFT JOIN review_ratings AS rr ON r.id = rr.review_id " +
            "GROUP BY r.id, r.content, r.is_positive, r.user_id, r.film_id " +
            "ORDER BY useful DESC ";

    private static final String FIND_TOP_QUERY = FIND_ALL_QUERY + "LIMIT ?";

    private static final String FIND_TOP_BY_FILM_ID = "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, " +
            "COALESCE(SUM(CASE WHEN rr.is_like = TRUE THEN 1 WHEN rr.is_like = FALSE THEN -1 ELSE 0 END), 0) AS useful " +
            "FROM reviews AS r " +
            "LEFT JOIN review_ratings AS rr ON r.id = rr.review_id " +
            "WHERE r.film_id = ? " +
            "GROUP BY r.id, r.content, r.is_positive, r.user_id, r.film_id " +
            "ORDER BY useful DESC " +
            "LIMIT ?";

    private static final String FIND_LIKE = "SELECT COUNT(*) FROM review_ratings WHERE review_id = ? AND user_id = ?";
    private static final String ADD_LIKE = "INSERT INTO review_ratings (review_id, user_id, is_like) VALUES (?, ?, ?)";
    private static final String UPDATE_LIKE = "UPDATE review_ratings SET is_like = ? WHERE review_id = ? AND user_id = ?";

    private static final String DELETE_LIKE_QUERY = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? ";
    private static final String DELETE_DISLIKE_QUERY = DELETE_LIKE_QUERY + "AND is_like = false";

    private final ReviewMapper mapper;
    private final JdbcTemplate jdbc;

    public List<Review> findAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public List<Review> findTop(long limit) {
        return jdbc.query(FIND_TOP_QUERY, mapper, limit);
    }

    public List<Review> findTopByFilmId(Long filmId, long limit) {
        return jdbc.query(FIND_TOP_BY_FILM_ID, mapper, filmId, limit);
    }

    public Optional<Review> findById(Long id) {
        try {
            String sql = "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, " +
                    "COALESCE(SUM(CASE WHEN rr.is_like = TRUE THEN 1 WHEN rr.is_like = FALSE THEN -1 ELSE 0 END), 0) AS useful " +
                    "FROM reviews AS r " +
                    "LEFT JOIN review_ratings AS rr ON r.id = rr.review_id " +
                    "WHERE r.id = ? " +
                    "GROUP BY r.id, r.content, r.is_positive, r.user_id, r.film_id";
            return Optional.ofNullable(jdbc.queryForObject(sql, mapper, id));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Long createReview(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (?, ?, ?, ?)";

        // returning generated key
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        return keyHolder.getKeyAs(Long.class);
    }

    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?;";

        jdbc.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getReviewId());

        return review;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM reviews WHERE id = ?;";
        jdbc.update(sql, id);
    }

    public void addLike(Long reviewId, Long userId) {
        Integer count = jdbc.queryForObject(FIND_LIKE, Integer.class, reviewId, userId);
        if (count != null && count > 0) {
            jdbc.update(UPDATE_LIKE, true, reviewId, userId);
        } else {
            jdbc.update(ADD_LIKE, reviewId, userId, true);
        }
    }

    public void addDislike(Long reviewId, Long userId) {
        Integer count = jdbc.queryForObject(FIND_LIKE, Integer.class, reviewId, userId);
        if (count != null && count > 0) {
            jdbc.update(UPDATE_LIKE, false, reviewId, userId);
        } else {
            jdbc.update(ADD_LIKE, reviewId, userId, false);
        }
    }

    public void deleteLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_LIKE_QUERY, reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        jdbc.update(DELETE_DISLIKE_QUERY, reviewId, userId);
    }
}