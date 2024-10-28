package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
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

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            int missingId = user == null ? userId : friendId;
            throw new NotFoundException("Пользователь с ID " + missingId + " не найден.");
        }

        if (!user.getFriends().contains(friendId)) {
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);

            userStorage.updateUser(user);
            userStorage.updateUser(friend);
        } else {
            log.info("Друг {} уже добавлен пользователю {}", friend.getName(), user.getName());
        }
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            int missingId = user == null ? userId : friendId;
            throw new NotFoundException("Пользователь с ID " + missingId + " не найден.");
        }

        if (!user.getFriends().contains(friendId)) {
            log.warn("Друг с ID {} не у вас в друзьях", friendId);
            return;
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
    }

    public List<User> getCommonFriends(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        List<User> commonFriends = new ArrayList<>();

        if (user == null || friend == null) {
            int missingId = user == null ? userId : friendId;
            throw new NotFoundException("Пользователь с ID " + missingId + " не найден.");
        }

        return user.getFriends().stream()
                .filter(friendIdValue -> friend.getFriends().contains(friendIdValue))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}