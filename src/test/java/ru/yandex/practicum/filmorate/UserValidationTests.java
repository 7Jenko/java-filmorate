package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTests {

    private UserController userController;
    private User validUser;
    private Validator validator;


    @BeforeEach
    void setUp() {
        // Инициализация валидатора
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);

        // Инициализация валидного пользователя
        validUser = new User(0, "user@example.com", "username", "User Name", LocalDate.of(2000, 1, 1));
    }

    @Test
    public void userWithValidUser() {
        User createdUser = userController.createUser(validUser);
        assert createdUser.getId() > 0;
    }

    @Test
    public void userWithEmptyEmail() {
        User userWithEmptyEmail = new User(0, "", "login", "Valid Name",
                LocalDate.of(2000, 1, 1));
        assertFalse(validator.validate(userWithEmptyEmail).isEmpty(), "Ожидалась ошибка: " +
                "Электронная почта не может быть пустой.");
    }

    @Test
    public void userWithInvalidEmail() {
        User userWithInvalidEmail = new User(0, "invalid-email", "login", "Valid Name",
                LocalDate.of(2000, 1, 1));
        assertFalse(validator.validate(userWithInvalidEmail).isEmpty(), "Ожидалась ошибка: " +
                "Электронная почта должна содержать символ @.");
    }

    @Test
    public void userWithEmptyLogin() {
        User userWithEmptyName = new User(0, "email@example.com", "login", null, LocalDate.of(2000, 1, 1));
        User createdUser = userController.createUser(userWithEmptyName);

        assertNotNull(createdUser, "Созданный пользователь не должен быть null");
        assertEquals("login", createdUser.getName(), "Если имя пустое, логин будет использован в качестве имени.");
    }

    @Test
    public void userWithInvalidLogin() {
        User userWithInvalidLogin = new User(0, "test@example.com", "log in", "Valid Name",
                LocalDate.of(2000, 1, 1)); // Логин содержит пробел
        assertFalse(validator.validate(userWithInvalidLogin).isEmpty(), "Ожидалась ошибка: " +
                "Логин не может содержать пробелы.");
    }

    @Test
    public void userWithFutureBirthday() {
        User userWithFutureBirthday = new User(0, "test@example.com", "validLogin", "Valid Name",
                LocalDate.of(3000, 1, 1));
        assertFalse(validator.validate(userWithFutureBirthday).isEmpty(), "Ожидалась ошибка: " +
                "Дата рождения не может быть в будущем.");
    }

    @Test
    public void userWithEmptyNameUsesLoginAsName() {
        User userWithEmptyName = new User(0, "test@example.com", "login", "",
                LocalDate.of(2000, 1, 1));
        userController.createUser(userWithEmptyName);
        assertEquals("login", userWithEmptyName.getName(), "Если имя пустое, " +
                "логин будет использован в качестве имени.");
    }
}
