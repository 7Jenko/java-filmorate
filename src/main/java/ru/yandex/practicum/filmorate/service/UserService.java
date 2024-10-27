package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final Map<Integer, List<Integer>> userFriends = new HashMap<>();

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

    public Map<String, String> addFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            int missingId = user == null ? userId : friendId;
            throw new NotFoundException("Пользователь с ID " + missingId + " не найден.");
        }

        if (user.getFriends().contains(friendId)) {
            return Map.of("Друг", friend.getName() + " уже у вас в друзьях");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        return Map.of("Друг", "Вы добавили " + friend.getName() + " в друзья");
    }

    public Map<String, String> removeFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            int missingId = user == null ? userId : friendId;
            throw new NotFoundException("Пользователь с ID " + missingId + " не найден.");
        }

        if (!user.getFriends().contains(friendId)) {
            return Map.of("Друг", friend.getName() + " не у вас в друзьях");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        return Map.of("Друг", "Вы удалили " + friend.getName() + " из друзей");
    }

    public List<User> getCommonFriends(int userId, int friendId) {
        List<Integer> friendsOfUser = userFriends.get(userId);
        List<Integer> friendsOfFriend = userFriends.get(friendId);
        List<User> commonFriends = new ArrayList<>();

        if (friendsOfUser != null && friendsOfFriend != null) {
            for (Integer friend : friendsOfUser) {
                if (friendsOfFriend.contains(friend)) {
                    commonFriends.add(userStorage.getUserById(friend));
                }
            }
        }
        return commonFriends;
    }

    public Set<Integer> getFriends(int userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user.getFriends();
    }
}