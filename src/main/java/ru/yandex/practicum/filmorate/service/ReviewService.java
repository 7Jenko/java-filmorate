package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

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
        eventService.createEvent(
                Math.toIntExact(review.getUserId()),
                EventType.REVIEW,
                EventOperation.ADD,
                Math.toIntExact(review.getReviewId())
        );

        return review;
    }

    public Review updateReview(RequestUpdateReviewDto reviewDto) {
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

        if (reviewDto.getIsPositive() != null) {
            review.setIsPositive(reviewDto.getIsPositive());
        }
        review = reviewStorage.updateReview(review);

        eventService.createEvent(
                Math.toIntExact(review.getUserId()),
                EventType.REVIEW,
                EventOperation.UPDATE,
                Math.toIntExact(review.getReviewId())
        );

        return review;
    }

    public void deleteById(Long id) {
        eventService.createEvent(
                Math.toIntExact(reviewStorage.findById(id).get().getUserId()),
                EventType.REVIEW,
                EventOperation.REMOVE,
                Math.toIntExact(id)
        );
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

    public void addLike(Long reviewId, Long userId, boolean isLike) {
        reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(
                        "Review with id = %d not found".formatted(reviewId)
                ));

        User user = userStorage.getUserById(Math.toIntExact(userId));
        if (user == null) {
            throw new NotFoundException("User with id = %d not found".formatted(userId));
        }

        reviewStorage.addLike(reviewId, userId, isLike);
    }

    public void deleteLike(Long reviewId, Long userId) {
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Review with id = %d not found".formatted(reviewId));
        }

        User user = userStorage.getUserById(Math.toIntExact(userId));
        if (user == null) {
            throw new NotFoundException("User with id = %d not found".formatted(userId));
        }

        reviewStorage.deleteLike(reviewId, userId);
    }
}