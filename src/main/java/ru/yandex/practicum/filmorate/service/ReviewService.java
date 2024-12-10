package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Review.RequestUpdateReviewDto;
import ru.yandex.practicum.filmorate.storage.Review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review createReview(Review review) {
        User user = userStorage.getUserById(Math.toIntExact(review.getUserId()));
        if (user == null) {
            throw new NotFoundException("User with id = %d not found".formatted(review.getUserId()));
        }

        Film film = filmStorage.getFilmById(Math.toIntExact(review.getFilmId()));
        if (film == null) {
            throw new NotFoundException("Film with id = %d not found".formatted(review.getFilmId()));
        }

        Long id = reviewStorage.createReview(review);

        review.setReviewId(id);
        review.setUseful(0L);

        return review;
    }

    public Review updateReview(@Valid RequestUpdateReviewDto reviewDto) {
        Review review = reviewStorage.findById(reviewDto.getReviewId())
                .orElseThrow(() -> new NotFoundException(
                        "Review with id = %d not found".formatted(reviewDto.getReviewId())
                ));

        User user = userStorage.getUserById(Math.toIntExact(review.getUserId()));
        if (user == null) {
            throw new NotFoundException("User with id = %d not found".formatted(review.getUserId()));
        }

        Film film = filmStorage.getFilmById(Math.toIntExact(review.getFilmId()));
        if (film == null) {
            throw new NotFoundException("Film with id = %d not found".formatted(review.getFilmId()));
        }

        if (reviewDto.getContent() != null) {
            if (reviewDto.getContent().isBlank()) {
                throw new ValidationException("Content cannot be blank");
            }

            review.setContent(reviewDto.getContent());
        }

        if (reviewDto.getUserId() != null) {
            review.setUserId(reviewDto.getUserId());
        }

        if (reviewDto.getFilmId() != null) {
            review.setFilmId(reviewDto.getFilmId());
        }

        if (reviewDto.getIsPositive() != null) {
            review.setIsPositive(reviewDto.getIsPositive());
        }

        return reviewStorage.updateReview(review);
    }

    public void deleteById(Long id) {
        reviewStorage.deleteById(id);
    }

    public Review findById(Long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Review with id = %d not found".formatted(id)
                ));
    }

    public List<Review> findTop(Long filmDd, long limit) {
        if (filmDd == null) {
            return reviewStorage.findTop(limit);
        } else {
            return reviewStorage.findTopByFilmId(filmDd, limit);
        }
    }

    public void addLike(Long reviewId, Long userId) {
        addLikeDislike(reviewId, userId, true);
    }

    public void addDislike(Long reviewId, Long userId) {
        addLikeDislike(reviewId, userId, false);
    }

    private void addLikeDislike(Long reviewId, Long userId, boolean isLike) {
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Review with id = %d not found".formatted(reviewId));
        }

        User user = userStorage.getUserById(Math.toIntExact(userId));
        if (user == null) {
            throw new NotFoundException("User with id = %d not found".formatted(userId));
        }

        if (isLike) {
            reviewStorage.addLike(reviewId, userId);
        } else {
            reviewStorage.addDislike(reviewId, userId);
        }
    }

    public void deleteLike(Long reviewId, Long userId) {
        deleteLikeDislike(reviewId, userId, true);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        deleteLikeDislike(reviewId, userId, false);
    }

    private void deleteLikeDislike(Long reviewId, Long userId, boolean isLike) {
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Review with id = %d not found".formatted(reviewId));
        }

        User user = userStorage.getUserById(Math.toIntExact(userId));
        if (user == null) {
            throw new NotFoundException("User with id = %d not found".formatted(userId));
        }

        if (isLike) {
            reviewStorage.deleteLike(reviewId, userId);
        } else {
            reviewStorage.deleteDislike(reviewId, userId);
        }
    }
}