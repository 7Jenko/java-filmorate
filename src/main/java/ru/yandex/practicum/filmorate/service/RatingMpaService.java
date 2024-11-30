package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.RatingMpaDbStorage;

import java.util.List;

@Service
public class RatingMpaService {

    private final RatingMpaDbStorage ratingMpaDbStorage;

    public RatingMpaService(RatingMpaDbStorage ratingMpaDbStorage) {
        this.ratingMpaDbStorage = ratingMpaDbStorage;
    }

    public RatingMpa getRatingMpaById(int id) {
        RatingMpa mpa = ratingMpaDbStorage.getRatingMpaById(id);
        if (mpa == null) {
            throw new NotFoundException("Рейтинг не найден");
        }
        return mpa;
    }

    public List<RatingMpa> getRatingsMpa() {
        return ratingMpaDbStorage.getRatingsMpa();
    }
}