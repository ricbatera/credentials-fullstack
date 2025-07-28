package br.com.consultdg.credential_portals_service.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordEncryptionServiceTest {

    private PasswordEncryptionService passwordEncryptionService;

    @BeforeEach
    void setUp() {
        passwordEncryptionService = new PasswordEncryptionService();
    }

    @Test
    void testEncryptPassword_ShouldReturnEncryptedPassword() {
        // Arrange
        String plainPassword = "minhasenha123";

        // Act
        String encryptedPassword = passwordEncryptionService.encryptPassword(plainPassword);

        // Assert
        assertNotNull(encryptedPassword);
        assertNotEquals(plainPassword, encryptedPassword);
        assertTrue(encryptedPassword.startsWith("$2a$"));
        assertEquals(60, encryptedPassword.length()); // BCrypt hash sempre tem 60 caracteres
    }

    @Test
    void testEncryptPassword_ShouldThrowExceptionForNullPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncryptionService.encryptPassword(null);
        });
    }

    @Test
    void testEncryptPassword_ShouldThrowExceptionForEmptyPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncryptionService.encryptPassword("");
        });
    }

    @Test
    void testEncryptPassword_ShouldThrowExceptionForBlankPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncryptionService.encryptPassword("   ");
        });
    }

    @Test
    void testVerifyPassword_ShouldReturnTrueForCorrectPassword() {
        // Arrange
        String plainPassword = "minhasenha123";
        String encryptedPassword = passwordEncryptionService.encryptPassword(plainPassword);

        // Act
        boolean result = passwordEncryptionService.verifyPassword(plainPassword, encryptedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void testVerifyPassword_ShouldReturnFalseForIncorrectPassword() {
        // Arrange
        String plainPassword = "minhasenha123";
        String wrongPassword = "senhaerrada";
        String encryptedPassword = passwordEncryptionService.encryptPassword(plainPassword);

        // Act
        boolean result = passwordEncryptionService.verifyPassword(wrongPassword, encryptedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyPassword_ShouldReturnFalseForNullPlainPassword() {
        // Arrange
        String encryptedPassword = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";

        // Act
        boolean result = passwordEncryptionService.verifyPassword(null, encryptedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void testVerifyPassword_ShouldReturnFalseForNullEncryptedPassword() {
        // Arrange
        String plainPassword = "minhasenha123";

        // Act
        boolean result = passwordEncryptionService.verifyPassword(plainPassword, null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsPasswordEncrypted_ShouldReturnTrueForValidBCryptHash() {
        // Arrange - Using a real BCrypt hash format
        String bcryptHash = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";

        // Act
        boolean result = passwordEncryptionService.isPasswordEncrypted(bcryptHash);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsPasswordEncrypted_ShouldReturnTrueForDifferentBCryptVariants() {
        // Arrange & Act & Assert - Using real BCrypt hash formats
        assertTrue(passwordEncryptionService.isPasswordEncrypted("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW"));
        assertTrue(passwordEncryptionService.isPasswordEncrypted("$2b$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW"));
        assertTrue(passwordEncryptionService.isPasswordEncrypted("$2x$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW"));
        assertTrue(passwordEncryptionService.isPasswordEncrypted("$2y$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW"));
    }

    @Test
    void testIsPasswordEncrypted_ShouldReturnFalseForPlainTextPassword() {
        // Arrange
        String plainPassword = "minhasenha123";

        // Act
        boolean result = passwordEncryptionService.isPasswordEncrypted(plainPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsPasswordEncrypted_ShouldReturnFalseForNullPassword() {
        // Act
        boolean result = passwordEncryptionService.isPasswordEncrypted(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsPasswordEncrypted_ShouldReturnFalseForInvalidHash() {
        // Arrange & Act & Assert
        assertFalse(passwordEncryptionService.isPasswordEncrypted("invalidhash"));
        assertFalse(passwordEncryptionService.isPasswordEncrypted("$2a$invalidhash"));
        assertFalse(passwordEncryptionService.isPasswordEncrypted("$1a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW"));
    }

    @Test
    void testEncryptPassword_ShouldGenerateDifferentHashesForSamePassword() {
        // Arrange
        String plainPassword = "minhasenha123";

        // Act
        String hash1 = passwordEncryptionService.encryptPassword(plainPassword);
        String hash2 = passwordEncryptionService.encryptPassword(plainPassword);

        // Assert
        assertNotEquals(hash1, hash2); // BCrypt usa salt aleatório
        assertTrue(passwordEncryptionService.verifyPassword(plainPassword, hash1));
        assertTrue(passwordEncryptionService.verifyPassword(plainPassword, hash2));
    }
}
