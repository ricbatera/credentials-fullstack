package br.com.consultdg.credential_portals_service.service;

import br.com.consultdg.credential_portals_service.api.model.ConsumerPublicKeyRequestDTO;
import br.com.consultdg.credential_portals_service.api.model.ConsumerPublicKeyResponseDTO;
import br.com.consultdg.credential_portals_service.model.ConsumerPublicKey;
import br.com.consultdg.credential_portals_service.repository.ConsumerPublicKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento das chaves públicas dos consumidores da API.
 */
@Service
public class ConsumerPublicKeyService {

    @Autowired
    private ConsumerPublicKeyRepository consumerPublicKeyRepository;

    @Autowired
    private RSAEncryptionService rsaEncryptionService;

    /**
     * Lista todas as chaves públicas ativas.
     */
    public List<ConsumerPublicKeyResponseDTO> findAll() {
        return consumerPublicKeyRepository.findAllActive()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma chave pública por ID.
     */
    public Optional<ConsumerPublicKeyResponseDTO> findById(UUID id) {
        return consumerPublicKeyRepository.findById(id)
                .filter(key -> key.getActive())
                .map(this::toResponseDTO);
    }

    /**
     * Busca uma chave pública por identificador do consumidor.
     */
    public Optional<ConsumerPublicKeyResponseDTO> findByConsumerIdentifier(String consumerIdentifier) {
        return consumerPublicKeyRepository.findByConsumerIdentifierAndActive(consumerIdentifier)
                .map(this::toResponseDTO);
    }

    /**
     * Busca uma chave pública válida (não expirada) por identificador do consumidor.
     */
    public Optional<ConsumerPublicKey> findValidKeyByConsumerIdentifier(String consumerIdentifier) {
        return consumerPublicKeyRepository.findValidKeyByConsumerIdentifier(consumerIdentifier);
    }

    /**
     * Registra uma nova chave pública para um consumidor.
     */
    public ConsumerPublicKeyResponseDTO registerPublicKey(ConsumerPublicKeyRequestDTO requestDTO) {
        // Valida se a chave pública é válida
        if (!rsaEncryptionService.isValidPublicKey(requestDTO.getPublicKey())) {
            throw new IllegalArgumentException("Chave pública RSA inválida");
        }

        // Verifica se já existe uma chave para este consumidor
        if (consumerPublicKeyRepository.existsByConsumerIdentifierAndActive(requestDTO.getConsumerIdentifier(), true)) {
            throw new IllegalArgumentException("Já existe uma chave ativa para este consumidor: " + requestDTO.getConsumerIdentifier());
        }

        ConsumerPublicKey consumerPublicKey = toEntity(requestDTO);
        consumerPublicKey = consumerPublicKeyRepository.save(consumerPublicKey);
        
        return toResponseDTO(consumerPublicKey);
    }

    /**
     * Atualiza uma chave pública existente.
     */
    public Optional<ConsumerPublicKeyResponseDTO> updatePublicKey(UUID id, ConsumerPublicKeyRequestDTO requestDTO) {
        return consumerPublicKeyRepository.findById(id)
                .filter(key -> key.getActive())
                .map(existingKey -> {
                    // Valida se a nova chave pública é válida
                    if (!rsaEncryptionService.isValidPublicKey(requestDTO.getPublicKey())) {
                        throw new IllegalArgumentException("Chave pública RSA inválida");
                    }

                    updateEntityFromDTO(existingKey, requestDTO);
                    return consumerPublicKeyRepository.save(existingKey);
                })
                .map(this::toResponseDTO);
    }

    /**
     * Remove (desativa) uma chave pública.
     */
    public boolean deletePublicKey(UUID id) {
        return consumerPublicKeyRepository.findById(id)
                .filter(key -> key.getActive())
                .map(key -> {
                    key.setActive(false);
                    consumerPublicKeyRepository.save(key);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Remove (desativa) uma chave pública por identificador do consumidor.
     */
    public boolean deletePublicKeyByConsumerIdentifier(String consumerIdentifier) {
        return consumerPublicKeyRepository.findByConsumerIdentifierAndActive(consumerIdentifier)
                .map(key -> {
                    key.setActive(false);
                    consumerPublicKeyRepository.save(key);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Verifica se um consumidor possui uma chave pública válida.
     */
    public boolean hasValidPublicKey(String consumerIdentifier) {
        return consumerPublicKeyRepository.findValidKeyByConsumerIdentifier(consumerIdentifier)
                .isPresent();
    }

    /**
     * Lista todas as chaves públicas válidas (não expiradas).
     */
    public List<ConsumerPublicKeyResponseDTO> findValidKeys() {
        return consumerPublicKeyRepository.findValidKeys()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gera um novo par de chaves RSA para demonstração.
     */
    public String generateKeyPairExample() {
        try {
            var keyPair = rsaEncryptionService.generateKeyPair();
            String publicKey = rsaEncryptionService.publicKeyToString(keyPair.getPublic());
            String privateKey = rsaEncryptionService.privateKeyToString(keyPair.getPrivate());
            
            return String.format(
                "# Par de Chaves RSA Gerado\n\n" +
                "## Chave Pública (registre na API):\n%s\n\n" +
                "## Chave Privada (mantenha em segredo):\n%s\n\n" +
                "## Como usar:\n" +
                "1. Registre a chave pública na API usando POST /api/consumer-keys\n" +
                "2. Use a chave privada para descriptografar senhas recebidas da API\n" +
                "3. Mantenha a chave privada segura e nunca a compartilhe",
                publicKey, privateKey
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar par de chaves: " + e.getMessage(), e);
        }
    }

    private ConsumerPublicKey toEntity(ConsumerPublicKeyRequestDTO requestDTO) {
        ConsumerPublicKey entity = new ConsumerPublicKey();
        entity.setConsumerName(requestDTO.getConsumerName());
        entity.setConsumerIdentifier(requestDTO.getConsumerIdentifier());
        entity.setPublicKey(requestDTO.getPublicKey());
        entity.setExpiresAt(requestDTO.getExpiresAt());
        entity.setDescription(requestDTO.getDescription());
        entity.setKeyAlgorithm("RSA");
        entity.setKeySize(2048);
        entity.setActive(true);
        return entity;
    }

    private void updateEntityFromDTO(ConsumerPublicKey entity, ConsumerPublicKeyRequestDTO requestDTO) {
        entity.setConsumerName(requestDTO.getConsumerName());
        entity.setPublicKey(requestDTO.getPublicKey());
        entity.setExpiresAt(requestDTO.getExpiresAt());
        entity.setDescription(requestDTO.getDescription());
    }

    private ConsumerPublicKeyResponseDTO toResponseDTO(ConsumerPublicKey entity) {
        return new ConsumerPublicKeyResponseDTO(
                entity.getId(),
                entity.getConsumerName(),
                entity.getConsumerIdentifier(),
                entity.getKeyAlgorithm(),
                entity.getKeySize(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getExpiresAt(),
                entity.getActive(),
                entity.getDescription(),
                entity.isValid()
        );
    }
}
