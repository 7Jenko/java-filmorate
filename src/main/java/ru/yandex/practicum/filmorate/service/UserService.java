package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void addFriend(int userId, int friendId) {
        userFriends.computeIfAbsent(userId, k -> new ArrayList<>()).add(friendId);
        userFriends.computeIfAbsent(friendId, k -> new ArrayList<>()).add(userId);

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user != null && friend != null) {
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
        }
    }

    public void removeFriend(int userId, int friendId) {
        log.debug("Удаление друга с ID {} у пользователя с ID {}", friendId, userId);
        List<Integer> friendsOfUser = userFriends.get(userId);
        if (friendsOfUser != null) {
            friendsOfUser.remove(Integer.valueOf(friendId));
        }
        List<Integer> friendsOfFriend = userFriends.get(friendId);
        if (friendsOfFriend != null) {
            friendsOfFriend.remove(Integer.valueOf(userId));
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user != null && friend != null) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        }
        log.info("Пользователь с ID {} удалил друга с ID {}", userId, friendId);
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
}