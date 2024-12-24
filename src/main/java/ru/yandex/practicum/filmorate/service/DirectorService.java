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
        log.info("Возвращаем  режиссера с id {}", directorId);
        return directorStorage.getById(directorId).orElseThrow(() ->
                new NotFoundException("Director with ID %d not found".formatted(directorId)));
    }

    public List<Director> getAll() {
        log.info("Возвращаем всех режиссеров");
        return directorStorage.getAll();
    }

    public Director create(Director director) {
        log.info("Создаем режиссера");
        director.setId(generateId());
        return directorStorage.create(director);
    }

    public Director update(Director newDirector) {
        log.info("Обновляем режиссера с id {}", newDirector.getId());
        if (directorStorage.getById(newDirector.getId()).isPresent()) {
            return directorStorage.update(newDirector);
        }
        log.warn("Не найден режиссер с id {}", newDirector.getId());
        throw new NotFoundException("Director not found!");
    }

    public void deleteById(Long directorId) {
        log.info("Удаляем режиссера с id {}", directorId);
        if (directorStorage.getById(directorId).isEmpty()) {
            log.warn("Режиссер с id не найден {}", directorId);
            throw new NotFoundException("Director not found!");
        }

        directorStorage.deleteById(directorId);
    }

    private Long generateId() {
        Long currentId = directorStorage.getAll().stream()
                .mapToLong(Director::getId)
                .max()
                .orElse(0);
        return ++currentId;
    }
}