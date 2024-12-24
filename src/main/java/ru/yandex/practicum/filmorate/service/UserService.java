package ru.yandex.practicum.filmorate.service;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final EventService eventService;

    @Autowired
    public UserService(UserStorage userStorage, EventService eventService) {
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    public User createUser(User user) {
        validationUserName(user);
        log.info("Добавление пользователя: {}", user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Integer userId, Integer friendId) {
        validateUsersExistence(userId, friendId);
        userStorage.addFriend(userId, friendId);

        log.info("Друг успешно добавлен: пользователь ID = {}, друг ID = {}", userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        validateUsersExistence(userId, friendId);
        userStorage.removeFriend(userId, friendId);

        log.info("Друг успешно удален: пользователь ID = {}, друг ID = {}", userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public List<User> getAllFriends(Integer userId) {
        checkUserExistence(userId);
        List<User> result = userStorage.getFriends(userId);
        log.info("Друзья пользователя с ID = {}: {}", userId, result);
        return result;
    }

    public List<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        validateUsersExistence(user1Id, user2Id);
        List<User> result = userStorage.getCommonFriends(user1Id, user2Id);
        log.info("Общие друзья пользователя с ID {} и {}: {}", user1Id, user2Id, result);
        return result;
    }

    public User getUserById(Integer id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    private void validateUsersExistence(Integer... userIds) {
        for (Integer userId : userIds) {
            userStorage.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        }
    }

    private void checkUserExistence(Integer userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    public void deleteUserById(Integer id) {
        userStorage.removeAllFriends(id);
        if (!userStorage.deleteUserById(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    public static void validationUserName(User user) {
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
            log.debug("В запросе на обновление пользователя с id={} отсутствует поле name, будет использовано поле login", user.getId());
        }
    }
}