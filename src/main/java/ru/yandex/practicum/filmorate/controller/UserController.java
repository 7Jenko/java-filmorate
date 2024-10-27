package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Запрос на создание пользователя: {}", user);
        User createdUser = userService.createUser(user);
        log.info("Пользователь создан: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public Map<String, String> addFriend(@PathVariable int userId, @PathVariable int friendId) {
        Map<String, String> response = userService.addFriend(userId, friendId);
        log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
        return response;
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public Map<String, String> removeFriend(@PathVariable int userId, @PathVariable int friendId) {
        Map<String, String> response = userService.removeFriend(userId, friendId);
        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
        return response;
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int userId, @PathVariable int otherId) {
        return userService.getCommonFriends(userId, otherId);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getFriends(@PathVariable int userId) {
        return (List<User>) userService.getFriends(userId);
    }
}
