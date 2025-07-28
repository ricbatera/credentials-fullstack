package br.com.consultdg.credential_portals_service.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respostas de chave pública do consumidor.
 */
@Schema(description = "DTO para resposta de chave pública do consumidor")
public class ConsumerPublicKeyResponseDTO {

    @Schema(description = "ID único da chave", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nome do consumidor", example = "Frontend Shopping App")
    private String consumerName;

    @Schema(description = "Identificador único do consumidor", example = "frontend-app-v1")
    private String consumerIdentifier;

    @Schema(description = "Algoritmo da chave", example = "RSA")
    private String keyAlgorithm;

    @Schema(description = "Tamanho da chave em bits", example = "2048")
    private Integer keySize;

    @Schema(description = "Data e hora de criação", example = "2025-07-26T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização", example = "2025-07-26T15:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Data de expiração da chave", example = "2025-12-31T23:59:59")
    private LocalDateTime expiresAt;

    @Schema(description = "Status ativo da chave", example = "true")
    private Boolean active;

    @Schema(description = "Descrição da chave", example = "Chave para aplicação frontend do shopping")
    private String description;

    @Schema(description = "Indica se a chave ainda é válida", example = "true")
    private Boolean isValid;

    // Constructors
    public ConsumerPublicKeyResponseDTO() {}

    public ConsumerPublicKeyResponseDTO(UUID id, String consumerName, String consumerIdentifier, 
                                       String keyAlgorithm, Integer keySize, LocalDateTime createdAt, 
                                       LocalDateTime updatedAt, LocalDateTime expiresAt, Boolean active, 
                                       String description, Boolean isValid) {
        this.id = id;
        this.consumerName = consumerName;
        this.consumerIdentifier = consumerIdentifier;
        this.keyAlgorithm = keyAlgorithm;
        this.keySize = keySize;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.active = active;
        this.description = description;
        this.isValid = isValid;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getConsumerIdentifier() {
        return consumerIdentifier;
    }

    public void setConsumerIdentifier(String consumerIdentifier) {
        this.consumerIdentifier = consumerIdentifier;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }
}
