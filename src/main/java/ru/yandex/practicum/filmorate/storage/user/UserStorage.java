package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendsStorage;

import java.util.List;

public interface UserStorage extends FriendsStorage {
    User createUser(User user);

    User updateUser(User user);

    User getUserById(int id);

    List<User> getAllUsers();

    String delete(int id);
}