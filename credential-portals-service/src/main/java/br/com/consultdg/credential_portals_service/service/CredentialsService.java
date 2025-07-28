package br.com.consultdg.credential_portals_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.consultdg.credential_portals_service.api.model.CredentialsRequestDTO;
import br.com.consultdg.credential_portals_service.api.model.CredentialsResponseDTO;
import br.com.consultdg.credential_portals_service.api.model.CredentialsWithEncryptedPasswordDTO;
import br.com.consultdg.credential_portals_service.model.Credentials;
import br.com.consultdg.credential_portals_service.model.ConsumerPublicKey;
import br.com.consultdg.credential_portals_service.repository.CredentialsRepository;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private PasswordEncryptionService passwordEncryptionService;

    @Autowired
    private RSAEncryptionService rsaEncryptionService;

    @Autowired
    private ConsumerPublicKeyService consumerPublicKeyService;

    @Autowired
    private InternalEncryptionService internalEncryptionService;

    public List<CredentialsResponseDTO> findAll() {
        return credentialsRepository.findAllActive()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<CredentialsResponseDTO> findById(UUID id) {
        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(this::toResponseDTO);
    }

    public CredentialsResponseDTO create(CredentialsRequestDTO requestDTO) {
        Credentials credentials = toEntity(requestDTO);
        encryptPasswordIfNeeded(credentials);
        credentials = credentialsRepository.save(credentials);
        return toResponseDTO(credentials);
    }

    public Optional<CredentialsResponseDTO> update(UUID id, CredentialsRequestDTO requestDTO) {
        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(existingCredentials -> {
                    updateEntityFromDTO(existingCredentials, requestDTO);
                    encryptPasswordIfNeeded(existingCredentials);
                    return credentialsRepository.save(existingCredentials);
                })
                .map(this::toResponseDTO);
    }

    public boolean delete(UUID id) {
        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(credentials -> {
                    credentials.setActive(false);
                    credentials.setDeletedAt(LocalDateTime.now());
                    credentialsRepository.save(credentials);
                    return true;
                })
                .orElse(false);
    }

    public Optional<CredentialsResponseDTO> findByCnpj(String cnpj) {
        return credentialsRepository.findByCnpjAndActive(cnpj)
                .map(this::toResponseDTO);
    }

    public List<CredentialsResponseDTO> findByNameMall(String nameMall) {
        return credentialsRepository.findByNameMallAndActive(nameMall)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Credentials toEntity(CredentialsRequestDTO requestDTO) {
        Credentials credentials = new Credentials();
        credentials.setNameMall(requestDTO.getNameMall());
        credentials.setCnpj(requestDTO.getCnpj());
        credentials.setUrlPortal(requestDTO.getUrlPortal());
        credentials.setUsername(requestDTO.getUsername());
        credentials.setPassword(requestDTO.getPassword());
        credentials.setPasswordOfInvoice(requestDTO.getPasswordOfInvoice());
        credentials.markPasswordAsChanged(); // Marca que a senha precisa ser criptografada
        credentials.setActive(requestDTO.getActive() != null ? requestDTO.getActive() : true);
        return credentials;
    }

    private void updateEntityFromDTO(Credentials credentials, CredentialsRequestDTO requestDTO) {
        credentials.setNameMall(requestDTO.getNameMall());
        credentials.setCnpj(requestDTO.getCnpj());
        credentials.setUrlPortal(requestDTO.getUrlPortal());
        credentials.setUsername(requestDTO.getUsername());
        credentials.setPassword(requestDTO.getPassword());
        credentials.setPasswordOfInvoice(requestDTO.getPasswordOfInvoice());
        credentials.setActive(requestDTO.getActive() != null ? requestDTO.getActive() : credentials.getActive());
    }

    private CredentialsResponseDTO toResponseDTO(Credentials credentials) {
        return new CredentialsResponseDTO(
                credentials.getId(),
                credentials.getNameMall(),
                credentials.getCnpj(),
                credentials.getUrlPortal(),
                credentials.getUsername(),
                credentials.getPasswordOfInvoice(),
                credentials.getCreatedAt(),
                credentials.getUpdatedAt(),
                credentials.getDeletedAt(),
                credentials.getActive()
        );
    }

    /**
     * Criptografa a senha se necessário antes de salvar no banco de dados.
     * 
     * @param credentials A entidade de credenciais
     */
    private void encryptPasswordIfNeeded(Credentials credentials) {
        if (credentials.getPassword() != null && 
            (credentials.isPasswordChanged() || !credentials.isPasswordEncrypted())) {
            
            // Salva uma cópia da senha original criptografada com AES (reversível)
            String originalPasswordEncrypted = internalEncryptionService.encrypt(credentials.getPassword());
            credentials.setOriginalPasswordEncrypted(originalPasswordEncrypted);
            
            // Criptografa a senha com BCrypt (irreversível para verificação)
            String encryptedPassword = passwordEncryptionService.encryptPassword(credentials.getPassword());
            credentials.setPassword(encryptedPassword);
            credentials.markPasswordAsProcessed();
        }
    }

    /**
     * Verifica se uma senha em texto plano corresponde à senha criptografada armazenada.
     * 
     * @param id ID da credencial
     * @param plainPassword Senha em texto plano
     * @return true se as senhas correspondem, false caso contrário
     */
    public boolean verifyPassword(UUID id, String plainPassword) {
        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(credential -> passwordEncryptionService.verifyPassword(plainPassword, credential.getPassword()))
                .orElse(false);
    }

    /**
     * Busca uma credencial por ID e retorna com a senha criptografada para o consumidor especificado.
     * 
     * @param id ID da credencial
     * @param consumerIdentifier Identificador do consumidor autorizado
     * @return CredentialsWithEncryptedPasswordDTO com senha criptografada
     * @throws IllegalArgumentException se o consumidor não possui chave pública válida
     */
    public Optional<CredentialsWithEncryptedPasswordDTO> findByIdWithEncryptedPassword(UUID id, String consumerIdentifier) {
        // Busca a chave pública válida do consumidor
        Optional<ConsumerPublicKey> consumerKey = consumerPublicKeyService.findValidKeyByConsumerIdentifier(consumerIdentifier);
        
        if (consumerKey.isEmpty()) {
            throw new IllegalArgumentException("Consumidor não possui chave pública válida: " + consumerIdentifier);
        }

        return credentialsRepository.findById(id)
                .filter(credential -> credential.getActive())
                .map(credential -> {
                    try {
                        // Descriptografa a senha original armazenada e criptografa com RSA para o consumidor
                        String originalPassword = internalEncryptionService.decrypt(credential.getOriginalPasswordEncrypted());
                        String encryptedForConsumer = rsaEncryptionService.encrypt(originalPassword, consumerKey.get().getPublicKey());
                        
                        // Criptografa também a senha da nota fiscal se ela existir
                        String encryptedPasswordOfInvoiceForConsumer = null;
                        if (credential.getPasswordOfInvoice() != null && !credential.getPasswordOfInvoice().isEmpty()) {
                            encryptedPasswordOfInvoiceForConsumer = rsaEncryptionService.encrypt(credential.getPasswordOfInvoice(), consumerKey.get().getPublicKey());
                        }
                        
                        return toEncryptedPasswordDTO(credential, encryptedForConsumer, encryptedPasswordOfInvoiceForConsumer, consumerIdentifier);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao criptografar senha para consumidor: " + e.getMessage(), e);
                    }
                });
    }

    /**
     * Lista todas as credenciais com senhas criptografadas para o consumidor especificado.
     * 
     * @param consumerIdentifier Identificador do consumidor autorizado
     * @return Lista de credenciais com senhas criptografadas
     */
    public List<CredentialsWithEncryptedPasswordDTO> findAllWithEncryptedPassword(String consumerIdentifier) {
        // Busca a chave pública válida do consumidor
        Optional<ConsumerPublicKey> consumerKey = consumerPublicKeyService.findValidKeyByConsumerIdentifier(consumerIdentifier);
        
        if (consumerKey.isEmpty()) {
            throw new IllegalArgumentException("Consumidor não possui chave pública válida: " + consumerIdentifier);
        }

        return credentialsRepository.findAllActive()
                .stream()
                .map(credential -> {
                    try {
                        // Descriptografa a senha original armazenada e criptografa com RSA para o consumidor
                        String originalPassword = internalEncryptionService.decrypt(credential.getOriginalPasswordEncrypted());
                        String encryptedForConsumer = rsaEncryptionService.encrypt(originalPassword, consumerKey.get().getPublicKey());
                        
                        // Criptografa também a senha da nota fiscal se ela existir
                        String encryptedPasswordOfInvoiceForConsumer = null;
                        if (credential.getPasswordOfInvoice() != null && !credential.getPasswordOfInvoice().isEmpty()) {
                            encryptedPasswordOfInvoiceForConsumer = rsaEncryptionService.encrypt(credential.getPasswordOfInvoice(), consumerKey.get().getPublicKey());
                        }
                        
                        return toEncryptedPasswordDTO(credential, encryptedForConsumer, encryptedPasswordOfInvoiceForConsumer, consumerIdentifier);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao criptografar senha para consumidor: " + e.getMessage(), e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Criptografa uma senha específica para um consumidor (usado para senhas em texto plano).
     * Este método é útil quando você tem a senha original e quer enviá-la criptografada.
     * 
     * @param plainPassword Senha em texto plano
     * @param consumerIdentifier Identificador do consumidor
     * @return Senha criptografada com a chave pública do consumidor
     */
    public String encryptPasswordForConsumer(String plainPassword, String consumerIdentifier) {
        Optional<ConsumerPublicKey> consumerKey = consumerPublicKeyService.findValidKeyByConsumerIdentifier(consumerIdentifier);
        
        if (consumerKey.isEmpty()) {
            throw new IllegalArgumentException("Consumidor não possui chave pública válida: " + consumerIdentifier);
        }

        try {
            return rsaEncryptionService.encrypt(plainPassword, consumerKey.get().getPublicKey());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar senha para consumidor: " + e.getMessage(), e);
        }
    }

    private CredentialsWithEncryptedPasswordDTO toEncryptedPasswordDTO(Credentials credentials, String encryptedPassword, String encryptedPasswordOfInvoice, String consumerIdentifier) {
        return new CredentialsWithEncryptedPasswordDTO(
                credentials.getId(),
                credentials.getNameMall(),
                credentials.getCnpj(),
                credentials.getUrlPortal(),
                credentials.getUsername(),
                encryptedPassword,
                encryptedPasswordOfInvoice,
                consumerIdentifier,
                "RSA",
                credentials.getCreatedAt(),
                credentials.getUpdatedAt(),
                credentials.getDeletedAt(),
                credentials.getActive()
        );
    }
}
