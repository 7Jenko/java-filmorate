package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director getById(Long directorId) {
        return directorStorage.getById(directorId).orElseThrow(() ->
                new NotFoundException("Director with ID %d not found".formatted(directorId)));
    }

    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director newDirector) {
        log.debug("Checking existence of director with ID {} for updating", newDirector.getId());
        if (directorStorage.getById(newDirector.getId()).isPresent()) {
            log.trace("Updating film in storage");
            return directorStorage.update(newDirector);
        }
        log.warn("Film with ID {} not found", newDirector.getId());
        throw new NotFoundException("Film not found!");
    }

    public void deleteById(Long directorId) {
        log.debug("Checking existence of director with ID {} for deleting", directorId);
        if (directorStorage.getById(directorId).isEmpty()) {
            log.warn("Director with ID {} not found", directorId);
            throw new NotFoundException("Director not found!");
        }
        log.trace("Deleting director in storage");
        directorStorage.deleteById(directorId);
    }
}