package com.axiserp.auth.domain.service;

import com.axiserp.auth.domain.exception.WeakPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordValidator Tests")
class PasswordValidatorTest {

    @Test
    @DisplayName("Should accept valid password with all requirements")
    void testValidPassword() {
        String validPassword = "ValidPass123!";
        assertDoesNotThrow(() -> PasswordValidator.validate(validPassword));
    }

    @Test
    @DisplayName("Should reject null password")
    void testNullPassword() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate(null)
        );
        assertTrue(exception.getMessage().contains("vacía"));
    }

    @Test
    @DisplayName("Should reject empty password")
    void testEmptyPassword() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("")
        );
        assertTrue(exception.getMessage().contains("vacía"));
    }

    @Test
    @DisplayName("Should reject password shorter than 8 characters")
    void testPasswordTooShort() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("Pass1!")
        );
        assertTrue(exception.getMessage().contains("entre 8 y 128"));
    }

    @Test
    @DisplayName("Should reject password longer than 128 characters")
    void testPasswordTooLong() {
        String longPassword = "Valid123!" + "A".repeat(120);
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate(longPassword)
        );
        assertTrue(exception.getMessage().contains("entre 8 y 128"));
    }

    @Test
    @DisplayName("Should reject password without uppercase letter")
    void testNoUppercase() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("validpass123!")
        );
        assertTrue(exception.getMessage().contains("mayúscula"));
    }

    @Test
    @DisplayName("Should reject password without lowercase letter")
    void testNoLowercase() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("VALIDPASS123!")
        );
        assertTrue(exception.getMessage().contains("minúscula"));
    }

    @Test
    @DisplayName("Should reject password without digit")
    void testNoDigit() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("ValidPass!")
        );
        assertTrue(exception.getMessage().contains("número"));
    }

    @Test
    @DisplayName("Should reject password without special character")
    void testNoSpecialCharacter() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("ValidPass123")
        );
        assertTrue(exception.getMessage().contains("carácter especial"));
    }

    @Test
    @DisplayName("Should reject password with spaces - CRITICAL BUG FIX")
    void testPasswordWithSpaces() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("Valid Pass123!")
        );
        assertTrue(exception.getMessage().contains("espacios"));
    }

    @Test
    @DisplayName("Should reject password with leading space")
    void testPasswordWithLeadingSpace() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate(" ValidPass123!")
        );
        assertTrue(exception.getMessage().contains("espacios"));
    }

    @Test
    @DisplayName("Should reject password with trailing space")
    void testPasswordWithTrailingSpace() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("ValidPass123! ")
        );
        assertTrue(exception.getMessage().contains("espacios"));
    }

    @Test
    @DisplayName("Should reject password with tab character")
    void testPasswordWithTab() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("ValidPass\t123!")
        );
        assertTrue(exception.getMessage().contains("espacios"));
    }

    @Test
    @DisplayName("Should reject password with newline character")
    void testPasswordWithNewline() {
        WeakPasswordException exception = assertThrows(
            WeakPasswordException.class,
            () -> PasswordValidator.validate("ValidPass\n123!")
        );
        assertTrue(exception.getMessage().contains("espacios"));
    }

    @Test
    @DisplayName("Should accept password with all special characters allowed")
    void testPasswordWithAllSpecialCharacters() {
        assertDoesNotThrow(() -> PasswordValidator.validate("Test@Pass123"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test#Pass123"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test$Pass123"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test%Pass123"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test^Pass123"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test&Pass123"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test*Pass123"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test!Pass123"));
    }

    @Test
    @DisplayName("Should accept password at minimum length boundary (8 chars)")
    void testPasswordMinimumLength() {
        assertDoesNotThrow(() -> PasswordValidator.validate("Aaa1bbb!"));
    }

    @Test
    @DisplayName("Should accept password at maximum length boundary (128 chars)")
    void testPasswordMaximumLength() {
        String maxPassword = "Valid123!" + "a".repeat(119);
        assertDoesNotThrow(() -> PasswordValidator.validate(maxPassword));
    }
}
