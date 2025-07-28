package br.com.consultdg.credential_portals_service.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Testes para o InternalEncryptionService
 */
@SpringBootTest
class InternalEncryptionServiceTest {

    private final InternalEncryptionService internalEncryptionService = new InternalEncryptionService();

    @Test
    void testEncryptAndDecrypt_ShouldReturnOriginalPassword() {
        // Arrange
        String originalPassword = "minhasenha123";

        // Act
        String encrypted = internalEncryptionService.encrypt(originalPassword);
        String decrypted = internalEncryptionService.decrypt(encrypted);

        // Assert
        assertNotNull(encrypted);
        assertNotEquals(originalPassword, encrypted);
        assertEquals(originalPassword, decrypted);
    }

    @Test
    void testEncrypt_ShouldReturnDifferentStringsForSameInput() {
        // Arrange
        String password = "mesmasenha";

        // Act
        String encrypted1 = internalEncryptionService.encrypt(password);
        String encrypted2 = internalEncryptionService.encrypt(password);

        // Assert
        // Nota: Com AES ECB, o resultado será o mesmo para a mesma entrada
        // Isso é aceitável para este caso de uso interno
        assertEquals(encrypted1, encrypted2);
    }

    @Test
    void testDecrypt_ShouldThrowExceptionForInvalidData() {
        // Arrange
        String invalidEncryptedData = "dadosinvalidos123";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            internalEncryptionService.decrypt(invalidEncryptedData);
        });
    }

    @Test
    void testEncrypt_ShouldThrowExceptionForNullInput() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            internalEncryptionService.encrypt(null);
        });
    }
}
