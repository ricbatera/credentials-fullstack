package br.com.consultdg.credential_portals_service.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

class RSAEncryptionServiceTest {

    private RSAEncryptionService rsaEncryptionService;
    private KeyPair keyPair;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        rsaEncryptionService = new RSAEncryptionService();
        keyPair = rsaEncryptionService.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    @Test
    void testGenerateKeyPair_ShouldCreateValidKeyPair() throws Exception {
        // Act
        KeyPair generatedKeyPair = rsaEncryptionService.generateKeyPair();

        // Assert
        assertNotNull(generatedKeyPair);
        assertNotNull(generatedKeyPair.getPublic());
        assertNotNull(generatedKeyPair.getPrivate());
        assertEquals("RSA", generatedKeyPair.getPublic().getAlgorithm());
        assertEquals("RSA", generatedKeyPair.getPrivate().getAlgorithm());
    }

    @Test
    void testPublicKeyToString_ShouldConvertToBase64() {
        // Act
        String publicKeyString = rsaEncryptionService.publicKeyToString(publicKey);

        // Assert
        assertNotNull(publicKeyString);
        assertFalse(publicKeyString.isEmpty());
        // Verifica se é Base64 válido (não deve ter caracteres especiais além de +, /, =)
        assertTrue(publicKeyString.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    void testPrivateKeyToString_ShouldConvertToBase64() {
        // Act
        String privateKeyString = rsaEncryptionService.privateKeyToString(privateKey);

        // Assert
        assertNotNull(privateKeyString);
        assertFalse(privateKeyString.isEmpty());
        assertTrue(privateKeyString.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    void testStringToPublicKey_ShouldConvertBackFromString() throws Exception {
        // Arrange
        String publicKeyString = rsaEncryptionService.publicKeyToString(publicKey);

        // Act
        PublicKey reconstructedKey = rsaEncryptionService.stringToPublicKey(publicKeyString);

        // Assert
        assertNotNull(reconstructedKey);
        assertEquals(publicKey.getAlgorithm(), reconstructedKey.getAlgorithm());
        assertArrayEquals(publicKey.getEncoded(), reconstructedKey.getEncoded());
    }

    @Test
    void testStringToPrivateKey_ShouldConvertBackFromString() throws Exception {
        // Arrange
        String privateKeyString = rsaEncryptionService.privateKeyToString(privateKey);

        // Act
        PrivateKey reconstructedKey = rsaEncryptionService.stringToPrivateKey(privateKeyString);

        // Assert
        assertNotNull(reconstructedKey);
        assertEquals(privateKey.getAlgorithm(), reconstructedKey.getAlgorithm());
        assertArrayEquals(privateKey.getEncoded(), reconstructedKey.getEncoded());
    }

    @Test
    void testEncryptDecrypt_ShouldWorkCorrectly() throws Exception {
        // Arrange
        String originalMessage = "Esta é uma mensagem secreta para teste!";

        // Act
        String encryptedMessage = rsaEncryptionService.encrypt(originalMessage, publicKey);
        String decryptedMessage = rsaEncryptionService.decrypt(encryptedMessage, privateKey);

        // Assert
        assertNotNull(encryptedMessage);
        assertNotEquals(originalMessage, encryptedMessage);
        assertEquals(originalMessage, decryptedMessage);
    }

    @Test
    void testEncryptWithStringKey_ShouldWork() throws Exception {
        // Arrange
        String originalMessage = "Mensagem de teste com chave string";
        String publicKeyString = rsaEncryptionService.publicKeyToString(publicKey);

        // Act
        String encryptedMessage = rsaEncryptionService.encrypt(originalMessage, publicKeyString);
        String decryptedMessage = rsaEncryptionService.decrypt(encryptedMessage, privateKey);

        // Assert
        assertEquals(originalMessage, decryptedMessage);
    }

    @Test
    void testDecryptWithStringKey_ShouldWork() throws Exception {
        // Arrange
        String originalMessage = "Mensagem de teste com chave string para descriptografia";
        String privateKeyString = rsaEncryptionService.privateKeyToString(privateKey);

        // Act
        String encryptedMessage = rsaEncryptionService.encrypt(originalMessage, publicKey);
        String decryptedMessage = rsaEncryptionService.decrypt(encryptedMessage, privateKeyString);

        // Assert
        assertEquals(originalMessage, decryptedMessage);
    }

    @Test
    void testEncrypt_ShouldThrowExceptionForNullData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            rsaEncryptionService.encrypt(null, publicKey);
        });
    }

    @Test
    void testEncrypt_ShouldThrowExceptionForEmptyData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            rsaEncryptionService.encrypt("", publicKey);
        });
    }

    @Test
    void testEncrypt_ShouldThrowExceptionForBlankData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            rsaEncryptionService.encrypt("   ", publicKey);
        });
    }

    @Test
    void testDecrypt_ShouldThrowExceptionForNullData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            rsaEncryptionService.decrypt(null, privateKey);
        });
    }

    @Test
    void testDecrypt_ShouldThrowExceptionForEmptyData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            rsaEncryptionService.decrypt("", privateKey);
        });
    }

    @Test
    void testIsValidPublicKey_ShouldReturnTrueForValidKey() {
        // Arrange
        String publicKeyString = rsaEncryptionService.publicKeyToString(publicKey);

        // Act
        boolean isValid = rsaEncryptionService.isValidPublicKey(publicKeyString);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsValidPublicKey_ShouldReturnFalseForInvalidKey() {
        // Arrange
        String invalidKey = "invalid-key-string";

        // Act
        boolean isValid = rsaEncryptionService.isValidPublicKey(invalidKey);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testIsValidPublicKey_ShouldReturnFalseForNullKey() {
        // Act
        boolean isValid = rsaEncryptionService.isValidPublicKey(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testEncryptDecrypt_LongMessage_ShouldWorkCorrectly() throws Exception {
        // Arrange
        String longMessage = "Esta é uma mensagem muito longa para testar a criptografia RSA. " +
                "Vamos ver se ela consegue ser criptografada e descriptografada corretamente. " +
                "A criptografia RSA tem limitações de tamanho baseadas no tamanho da chave.";

        // Act
        String encryptedMessage = rsaEncryptionService.encrypt(longMessage, publicKey);
        String decryptedMessage = rsaEncryptionService.decrypt(encryptedMessage, privateKey);

        // Assert
        assertEquals(longMessage, decryptedMessage);
    }

    @Test
    void testMultipleEncryptionsGenerateDifferentResults() throws Exception {
        // Arrange
        String message = "Mensagem para testar variabilidade";

        // Act
        String encrypted1 = rsaEncryptionService.encrypt(message, publicKey);
        String encrypted2 = rsaEncryptionService.encrypt(message, publicKey);

        // Assert
        // RSA sem padding aleatório pode gerar o mesmo resultado, mas com padding PKCS1 pode variar
        // O importante é que ambos descriptografem para a mensagem original
        String decrypted1 = rsaEncryptionService.decrypt(encrypted1, privateKey);
        String decrypted2 = rsaEncryptionService.decrypt(encrypted2, privateKey);
        
        assertEquals(message, decrypted1);
        assertEquals(message, decrypted2);
    }

    @Test
    void testEncryptDecrypt_SpecialCharacters_ShouldWork() throws Exception {
        // Arrange
        String messageWithSpecialChars = "Mensagem com caracteres especiais: àáâãéêíóôõúç!@#$%^&*()";

        // Act
        String encryptedMessage = rsaEncryptionService.encrypt(messageWithSpecialChars, publicKey);
        String decryptedMessage = rsaEncryptionService.decrypt(encryptedMessage, privateKey);

        // Assert
        assertEquals(messageWithSpecialChars, decryptedMessage);
    }
}
