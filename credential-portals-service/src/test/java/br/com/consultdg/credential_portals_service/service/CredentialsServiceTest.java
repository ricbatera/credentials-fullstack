package br.com.consultdg.credential_portals_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.consultdg.credential_portals_service.api.model.CredentialsRequestDTO;
import br.com.consultdg.credential_portals_service.api.model.CredentialsResponseDTO;
import br.com.consultdg.credential_portals_service.model.Credentials;
import br.com.consultdg.credential_portals_service.repository.CredentialsRepository;

@ExtendWith(MockitoExtension.class)
class CredentialsServiceTest {

    @Mock
    private CredentialsRepository credentialsRepository;

    @Mock
    private PasswordEncryptionService passwordEncryptionService;

    @Mock
    private InternalEncryptionService internalEncryptionService;

    @InjectMocks
    private CredentialsService credentialsService;

    private CredentialsRequestDTO requestDTO;
    private Credentials credentials;
    private final String PLAIN_PASSWORD = "minhasenha123";
    private final String ENCRYPTED_PASSWORD = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private final String INTERNAL_ENCRYPTED_PASSWORD = "AES_ENCRYPTED_PASSWORD";

    @BeforeEach
    void setUp() {
        requestDTO = new CredentialsRequestDTO();
        requestDTO.setNameMall("Test Mall");
        requestDTO.setCnpj("12345678901234");
        requestDTO.setUrlPortal("https://test.portal.com");
        requestDTO.setUsername("testuser");
        requestDTO.setPassword(PLAIN_PASSWORD);
        requestDTO.setPasswordOfInvoice("testInvoicePassword");
        requestDTO.setActive(true);

        credentials = new Credentials();
        credentials.setId(UUID.randomUUID());
        credentials.setNameMall("Test Mall");
        credentials.setCnpj("12345678901234");
        credentials.setUrlPortal("https://test.portal.com");
        credentials.setUsername("testuser");
        credentials.setPassword(ENCRYPTED_PASSWORD);
        credentials.setPasswordOfInvoice("testInvoicePassword");
        credentials.setOriginalPasswordEncrypted(INTERNAL_ENCRYPTED_PASSWORD);
        credentials.setActive(true);
    }

    @Test
    void testCreateCredentials_ShouldEncryptPassword() {
        // Arrange
        when(internalEncryptionService.encrypt(PLAIN_PASSWORD))
                .thenReturn(INTERNAL_ENCRYPTED_PASSWORD);
        when(passwordEncryptionService.encryptPassword(PLAIN_PASSWORD))
                .thenReturn(ENCRYPTED_PASSWORD);
        when(credentialsRepository.save(any(Credentials.class)))
                .thenReturn(credentials);

        // Act
        CredentialsResponseDTO result = credentialsService.create(requestDTO);

        // Assert
        assertNotNull(result);
        verify(internalEncryptionService).encrypt(PLAIN_PASSWORD);
        verify(passwordEncryptionService).encryptPassword(PLAIN_PASSWORD);
        verify(credentialsRepository).save(any(Credentials.class));
    }

    @Test
    void testUpdateCredentials_ShouldEncryptNewPassword() {
        // Arrange
        UUID id = UUID.randomUUID();
        Credentials existingCredentials = new Credentials();
        existingCredentials.setId(id);
        existingCredentials.setPassword("$2a$12$oldencryptedpasswordR9h/cIPz0gi.URNNX3kh2OPST9/PgBkq");
        existingCredentials.setActive(true);

        when(credentialsRepository.findById(id))
                .thenReturn(Optional.of(existingCredentials));
        when(internalEncryptionService.encrypt(PLAIN_PASSWORD))
                .thenReturn(INTERNAL_ENCRYPTED_PASSWORD);
        when(passwordEncryptionService.encryptPassword(PLAIN_PASSWORD))
                .thenReturn(ENCRYPTED_PASSWORD);
        when(credentialsRepository.save(any(Credentials.class)))
                .thenReturn(existingCredentials);

        // Act
        Optional<CredentialsResponseDTO> result = credentialsService.update(id, requestDTO);

        // Assert
        assertTrue(result.isPresent());
        verify(internalEncryptionService).encrypt(PLAIN_PASSWORD);
        verify(passwordEncryptionService).encryptPassword(PLAIN_PASSWORD);
        verify(credentialsRepository).save(any(Credentials.class));
    }

    @Test
    void testVerifyPassword_ShouldReturnTrueForCorrectPassword() {
        // Arrange
        UUID id = UUID.randomUUID();
        credentials.setId(id);
        
        when(credentialsRepository.findById(id))
                .thenReturn(Optional.of(credentials));
        when(passwordEncryptionService.verifyPassword(PLAIN_PASSWORD, ENCRYPTED_PASSWORD))
                .thenReturn(true);

        // Act
        boolean result = credentialsService.verifyPassword(id, PLAIN_PASSWORD);

        // Assert
        assertTrue(result);
        verify(passwordEncryptionService).verifyPassword(PLAIN_PASSWORD, ENCRYPTED_PASSWORD);
    }

    @Test
    void testVerifyPassword_ShouldReturnFalseForIncorrectPassword() {
        // Arrange
        UUID id = UUID.randomUUID();
        credentials.setId(id);
        String wrongPassword = "senhaerrada";
        
        when(credentialsRepository.findById(id))
                .thenReturn(Optional.of(credentials));
        when(passwordEncryptionService.verifyPassword(wrongPassword, ENCRYPTED_PASSWORD))
                .thenReturn(false);

        // Act
        boolean result = credentialsService.verifyPassword(id, wrongPassword);

        // Assert
        assertFalse(result);
        verify(passwordEncryptionService).verifyPassword(wrongPassword, ENCRYPTED_PASSWORD);
    }

    @Test
    void testVerifyPassword_ShouldReturnFalseForNonExistentCredential() {
        // Arrange
        UUID id = UUID.randomUUID();
        
        when(credentialsRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act
        boolean result = credentialsService.verifyPassword(id, PLAIN_PASSWORD);

        // Assert
        assertFalse(result);
        verify(passwordEncryptionService, never()).verifyPassword(anyString(), anyString());
    }
}
