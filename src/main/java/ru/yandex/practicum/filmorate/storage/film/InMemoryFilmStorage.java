package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;

    @Override
    public Film createFilm(Film film) {
        Duration duration = Duration.ofMinutes(film.getDuration());
        film.setId(++currentId);
        films.put(film.getId(), film);
        log.info("Создан фильм с ID: {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        int id = film.getId();
        if (!films.containsKey(id)) {
            log.error("Не найден фильм с ID: {}", id);
            throw new NotFoundException("Не найден фильм с id " + id);
        }
        Film updatedFilm = films.get(id);
        updatedFilm.setName(film.getName());
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());
        log.info("Обновлен фильм с ID: {}", id);
        return updatedFilm;
    }

    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void removeFilm(int id) {
        films.remove(id);
    }
}