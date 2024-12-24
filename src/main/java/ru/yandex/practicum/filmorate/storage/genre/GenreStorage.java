package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import java.util.Map;

public interface GenreStorage {

    void deleteAllGenresById(int filmId);

    Genre getGenreById(int genreId);

    List<Genre> getAllGenres();

    Map<Integer, List<Genre>> getGenresByFilmIds(List<Integer> filmIds);
}