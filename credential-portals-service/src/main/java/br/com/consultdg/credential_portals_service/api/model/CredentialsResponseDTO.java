package br.com.consultdg.credential_portals_service.api.model;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para respostas de credenciais")
public class CredentialsResponseDTO {

    @Schema(description = "ID único da credencial", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nome do shopping center", example = "Shopping Center Norte")
    private String nameMall;

    @Schema(description = "CNPJ da empresa", example = "12345678000195")
    private String cnpj;

    @Schema(description = "URL do portal", example = "https://portal.shopping.com.br")
    private String urlPortal;

    @Schema(description = "Nome de usuário para acesso", example = "admin")
    private String username;

    @Schema(description = "Senha da nota fiscal", example = "senhaNotaFiscal123")
    private String passwordOfInvoice;

    @Schema(description = "Data e hora de criação", example = "2025-07-24T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização", example = "2025-07-24T15:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Data e hora de exclusão (soft delete)", example = "null")
    private LocalDateTime deletedAt;

    @Schema(description = "Status ativo da credencial", example = "true")
    private Boolean active;

    // Constructors
    public CredentialsResponseDTO() {
    }

    public CredentialsResponseDTO(UUID id, String nameMall, String cnpj, String urlPortal, String username, 
                                  String passwordOfInvoice, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, Boolean active) {
        this.id = id;
        this.nameMall = nameMall;
        this.cnpj = cnpj;
        this.urlPortal = urlPortal;
        this.username = username;
        this.passwordOfInvoice = passwordOfInvoice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.active = active;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNameMall() {
        return nameMall;
    }

    public void setNameMall(String nameMall) {
        this.nameMall = nameMall;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getUrlPortal() {
        return urlPortal;
    }

    public void setUrlPortal(String urlPortal) {
        this.urlPortal = urlPortal;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordOfInvoice() {
        return passwordOfInvoice;
    }

    public void setPasswordOfInvoice(String passwordOfInvoice) {
        this.passwordOfInvoice = passwordOfInvoice;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
