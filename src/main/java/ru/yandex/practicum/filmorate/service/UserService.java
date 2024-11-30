package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        checkUser(userId, userId);
        List<User> result = userStorage.getFriends(userId);
        log.info("Друзья пользователя с ID = " + userId + result);
        return result;
    }

    public List<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        checkUser(user1Id, user2Id);
        List<User> result = userStorage.getCommonFriends(user1Id, user2Id);
        log.info("Общие друзья пользователя с ID " + " {} и {} {} ", user1Id, user2Id, result);
        return result;
    }

    private void checkUser(Integer userId, Integer friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
    }
}