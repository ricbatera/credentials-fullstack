package br.com.consultdg.credential_portals_service.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para requisição de verificação de senha.
 */
@Schema(description = "Dados para verificação de senha")
public class PasswordVerificationRequestDTO {

    @Schema(description = "Senha em texto plano", example = "minhasenha123", required = true)
    private String password;

    public PasswordVerificationRequestDTO() {
    }

    public PasswordVerificationRequestDTO(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
