package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;

public interface GenreStorage {

    void deleteAllGenresById(int filmId);

    Genre getGenreById(int genreId);

    List<Genre> getAllGenres();
}