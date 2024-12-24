package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface LikeStorage {
    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    List<Film> getPopular(Integer count);

    boolean existsLike(int filmId, int userId);
}