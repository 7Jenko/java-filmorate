package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        log.debug("Добавление пользователя: {}", user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Integer userId, Integer friendId) {
        checkUser(userId, friendId);
        userStorage.addFriend(userId, friendId);

        log.info("Друг успешно добавлен");
    }

    public void removeFriend(Integer userId, Integer friendId) {
        checkUser(userId, friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("Друг успешно удален");
    }

    public List<User> getAllFriends(Integer userId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        List<User> result = userStorage.getFriends(userId);
        log.info("Друзья пользователя с ID = " + userId + result);
        return result;
    }

    public List<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        if (!existsById(user1Id) || !existsById(user2Id)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        List<User> result = userStorage.getCommonFriends(user1Id, user2Id);
        log.info("Общие друзья пользователя с ID " + " {} и {} {} ", user1Id, user2Id, result);
        return result;
    }

    private void checkUser(Integer userId, Integer friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
    }

    public void deleteById(int userId) {
        log.debug("Попытка удалить пользователя с ID {}", userId);
        if (userStorage.getUserById(userId) == null) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        log.trace("Удаление пользователя ID {}", userId);
        userStorage.deleteById(userId);
        log.info("Успешно удалён пользователь с ID {}", userId);
    }

    public void removeAllFriends(Integer userId) {
        log.trace("Получение списка друзей для пользователя с ID: {}", userId);
        List<Integer> friendIds = userStorage.getFriendIdsByUserId(userId);

        if (friendIds.isEmpty()) {
            log.info("У пользователя с ID {} нет друзей.", userId);
            return; // Если друзей нет, выходим из метода
        }

        for (Integer friendId : friendIds) {
            removeFriend(userId, friendId);
        }
        log.info("Все друзья пользователя с ID {} удалены.", userId);
    }

    public boolean existsById(Integer userId) {
        return userStorage.getUserById(userId) != null;
    }
}