package br.com.consultdg.credential_portals_service.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para requisições de credenciais")
public class CredentialsRequestDTO {

    @Schema(description = "Nome do shopping center", example = "Shopping Center Norte", required = true)
    private String nameMall;

    @Schema(description = "CNPJ da empresa", example = "12345678000195")
    private String cnpj;

    @Schema(description = "URL do portal", example = "https://portal.shopping.com.br", required = true)
    private String urlPortal;

    @Schema(description = "Nome de usuário para acesso", example = "admin", required = true)
    private String username;

    @Schema(description = "Senha para acesso", example = "senha123", required = true)
    private String password;

    @Schema(description = "Status ativo da credencial", example = "true", required = true)
    private Boolean active;

    // Constructors
    public CredentialsRequestDTO() {
    }

    public CredentialsRequestDTO(String nameMall, String cnpj, String urlPortal, String username, String password, Boolean active) {
        this.nameMall = nameMall;
        this.cnpj = cnpj;
        this.urlPortal = urlPortal;
        this.username = username;
        this.password = password;
        this.active = active;
    }

    // Getters and Setters
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
        this.password = password;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
