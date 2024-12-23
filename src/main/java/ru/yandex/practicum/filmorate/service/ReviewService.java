package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.Review.RequestUpdateReviewDto;
import ru.yandex.practicum.filmorate.storage.Review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage, FilmStorage filmStorage, EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventService = eventService;
    }

    public Review createReview(Review review) {
        int userId = review.getUserId().intValue();
        int filmId = review.getFilmId().intValue();

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = %d not found".formatted(review.getUserId())));

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id = %d not found".formatted(review.getFilmId())));

        Long id = reviewStorage.createReview(review);
        review.setReviewId(id);
        review.setUseful(0L);

        eventService.createEvent(userId, EventType.REVIEW, EventOperation.ADD, id.intValue());

        return review;
    }

    public Review updateReview(RequestUpdateReviewDto reviewDto) {
        Review review = reviewStorage.findById(reviewDto.getReviewId())
                .orElseThrow(() -> new NotFoundException("Review with id = %d not found".formatted(reviewDto.getReviewId())));

        int userId = review.getUserId().intValue();
        int filmId = review.getFilmId().intValue();

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = %d not found".formatted(userId)));

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id = %d not found".formatted(filmId)));

        if (reviewDto.getContent() != null) {
            if (reviewDto.getContent().isBlank()) {
                throw new ValidationException("Content cannot be blank");
            }
            review.setContent(reviewDto.getContent());
        }

        if (reviewDto.getIsPositive() != null) {
            review.setIsPositive(reviewDto.getIsPositive());
        }

        review = reviewStorage.updateReview(review);

        eventService.createEvent(userId, EventType.REVIEW, EventOperation.UPDATE, review.getReviewId().intValue());

        return review;
    }

    public void deleteById(Long id) {
        Review review = reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Review with id = %d not found".formatted(id)));

        eventService.createEvent(review.getUserId().intValue(), EventType.REVIEW, EventOperation.REMOVE, id.intValue());
        reviewStorage.deleteById(id);
    }

    public Review findById(Long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Review with id = %d not found".formatted(id)));
    }

    public List<Review> findTop(Long filmId, long limit) {
        if (filmId == null) {
            return reviewStorage.findTop(limit);
        } else {
            return reviewStorage.findTopByFilmId(filmId, limit);
        }
    }

    public void addLike(Long reviewId, Long userId, boolean isLike) {
        reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with id = %d not found".formatted(reviewId)));

        User user = userStorage.getUserById(userId.intValue())
                .orElseThrow(() -> new NotFoundException("User with id = %d not found".formatted(userId)));

        reviewStorage.addLike(reviewId, userId, isLike);
    }

    public void deleteLike(Long reviewId, Long userId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new NotFoundException("Review with ID " + reviewId + " not found.");
        }

        User user = userStorage.getUserById(userId.intValue())
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found."));

        reviewStorage.deleteLike(reviewId, userId);
    }
}