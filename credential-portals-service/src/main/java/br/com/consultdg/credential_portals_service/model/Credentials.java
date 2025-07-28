package br.com.consultdg.credential_portals_service.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "credentials")
public class Credentials {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "name_mall", nullable = false, length = 255)
    private String nameMall;
    
    @Column(name = "cnpj", nullable = true, length = 18)
    private String cnpj;
    
    @Column(name = "url_portal", nullable = false, length = 500)
    private String urlPortal;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "original_password_encrypted", nullable = true, length = 1000)
    private String originalPasswordEncrypted;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "password_of_invoice", nullable = true, length = 255)
    private String passwordOfInvoice;

    @Transient
    private boolean passwordChanged = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public String getPasswordOfInvoice() {
        return passwordOfInvoice;
    }
    
    public void setPasswordOfInvoice(String passwordOfInvoice) {
        this.passwordOfInvoice = passwordOfInvoice;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && !password.equals(this.password)) {
            this.passwordChanged = true;
        }
        this.password = password;
    }

    public String getOriginalPasswordEncrypted() {
        return originalPasswordEncrypted;
    }

    public void setOriginalPasswordEncrypted(String originalPasswordEncrypted) {
        this.originalPasswordEncrypted = originalPasswordEncrypted;
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

    /**
     * Verifica se a senha já está criptografada baseado no padrão do BCrypt.
     */
    public boolean isPasswordEncrypted() {
        if (this.password == null) {
            return false;
        }
        // BCrypt hash sempre começa com $2a$, $2b$, $2x$ ou $2y$ seguido do custo e tem 60 caracteres
        return this.password.matches("^\\$2[abxy]\\$\\d{2}\\$.{53}$");
    }

    /**
     * Marca que a senha foi alterada e precisa ser criptografada.
     */
    public void markPasswordAsChanged() {
        this.passwordChanged = true;
    }

    /**
     * Verifica se a senha foi alterada e precisa ser criptografada.
     */
    public boolean isPasswordChanged() {
        return this.passwordChanged;
    }

    /**
     * Marca que a senha foi processada (criptografada).
     */
    public void markPasswordAsProcessed() {
        this.passwordChanged = false;
    }
}
