package br.com.consultdg.credential_portals_service.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para requisições de registro de chave pública do consumidor.
 */
@Schema(description = "DTO para registro de chave pública do consumidor")
public class ConsumerPublicKeyRequestDTO {

    @Schema(description = "Nome do consumidor", example = "Frontend Shopping App", required = true)
    private String consumerName;

    @Schema(description = "Identificador único do consumidor", example = "frontend-app-v1", required = true)
    private String consumerIdentifier;

    @Schema(description = "Chave pública RSA em formato Base64", required = true)
    private String publicKey;

    @Schema(description = "Data de expiração da chave (opcional)", example = "2025-12-31T23:59:59")
    private LocalDateTime expiresAt;

    @Schema(description = "Descrição da chave", example = "Chave para aplicação frontend do shopping")
    private String description;

    // Constructors
    public ConsumerPublicKeyRequestDTO() {}

    public ConsumerPublicKeyRequestDTO(String consumerName, String consumerIdentifier, String publicKey) {
        this.consumerName = consumerName;
        this.consumerIdentifier = consumerIdentifier;
        this.publicKey = publicKey;
    }

    // Getters and Setters
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
