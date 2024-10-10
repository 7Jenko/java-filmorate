package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 1;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        try {
            validateLogin(user);
            user.setId(currentId++);
            users.put(user.getId(), user);
            log.info("Создан пользователь с ID: {}", user.getId());
            return user;
        } catch (ValidationException exp) {
            log.error("Ошибка валидации при создании пользователя: {}", exp.getMessage());
            throw exp;
        }
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable int id, @Valid @RequestBody User user) {
        if (!users.containsKey(id)) {
            log.error("Не найден пользователь с ID: {}", id);
            throw new ValidationException("Не найден пользователь с id " + id);
        }
        try {
        validateLogin(user);
        User updatedUser = users.get(id);
        updatedUser.setEmail(user.getEmail());
        updatedUser.setLogin(user.getLogin());
        updatedUser.setBirthday(user.getBirthday());
        log.info("Обновлен пользователь с ID: {}", id);
        return updatedUser;
        } catch (ValidationException exp) {
            log.error("Ошибка валидации при обновлении пользователя: {}", exp.getMessage());
            throw exp;
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private void validateLogin(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Имя пользователя не задано, будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
