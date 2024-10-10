package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTests {

    private UserController userController;
    private User validUser;
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setValidUser() {
        userController = new UserController();
        validUser = new User(0, "test@example.com", "validLogin", "Valid Name",
                LocalDate.of(2000, 1, 1));
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
        User userWithEmptyLogin = new User(0, "test@example.com", "", "Valid Name",
                LocalDate.of(2000, 1, 1));
        assertFalse(validator.validate(userWithEmptyLogin).isEmpty(), "Ожидалась ошибка: " +
                "Логин не может быть пустым.");
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
