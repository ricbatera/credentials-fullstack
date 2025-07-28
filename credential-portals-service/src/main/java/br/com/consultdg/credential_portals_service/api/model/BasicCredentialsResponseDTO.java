package br.com.consultdg.credential_portals_service.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para retornar credenciais básicas para robôs consumidores
 */
@Schema(description = "Credenciais básicas para consumo por robôs")
public class BasicCredentialsResponseDTO {

    @Schema(description = "URL do portal", example = "https://portal.shopping.com")
    private String urlPortal;

    @Schema(description = "Nome de usuário para acesso", example = "usuario123")
    private String username;

    @Schema(description = "Senha para acesso", example = "senhaSegura123")
    private String password;

    @Schema(description = "Senha da nota fiscal", example = "senhaNotaFiscal123")
    private String passwordOfInvoice;

    @Schema(description = "Nome do shopping/mall", example = "Shopping Center ABC")
    private String nameMall;

    // Constructors
    public BasicCredentialsResponseDTO() {}

    public BasicCredentialsResponseDTO(String urlPortal, String username, String password, String passwordOfInvoice, String nameMall) {
        this.urlPortal = urlPortal;
        this.username = username;
        this.password = password;
        this.passwordOfInvoice = passwordOfInvoice;
        this.nameMall = nameMall;
    }

    // Getters and Setters
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
        this.password = password;
    }

    public String getPasswordOfInvoice() {
        return passwordOfInvoice;
    }

    public void setPasswordOfInvoice(String passwordOfInvoice) {
        this.passwordOfInvoice = passwordOfInvoice;
    }

    public String getNameMall() {
        return nameMall;
    }

    public void setNameMall(String nameMall) {
        this.nameMall = nameMall;
    }
}
