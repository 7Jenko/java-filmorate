package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 0;

    @Override
    public User createUser(User user) {
        validateLogin(user);
        user.setId(++currentId);
        users.put(user.getId(), user);
        log.info("Создан пользователь с ID: {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        int id = user.getId();
        if (!users.containsKey(id)) {
            log.error("Не найден пользователь с ID: {}", id);
            throw new NotFoundException("Не найден пользователь с id " + id);
        }
        validateLogin(user);
        User updatedUser = users.get(id);
        updatedUser.setName(user.getName());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setLogin(user.getLogin());
        updatedUser.setBirthday(user.getBirthday());
        log.info("Обновлен пользователь с ID: {}", id);
        return updatedUser;
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }

    private void validateLogin(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Имя пользователя не задано, будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}