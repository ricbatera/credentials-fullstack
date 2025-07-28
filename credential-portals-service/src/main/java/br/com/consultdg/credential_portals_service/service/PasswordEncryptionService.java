package br.com.consultdg.credential_portals_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela criptografia e verificação de senhas.
 * Utiliza o algoritmo BCrypt para garantir a segurança das senhas armazenadas.
 */
@Service
public class PasswordEncryptionService {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncryptionService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12); // Força 12 (mais seguro)
    }

    /**
     * Criptografa uma senha em texto plano.
     * 
     * @param plainPassword A senha em texto plano
     * @return A senha criptografada
     */
    public String encryptPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia");
        }
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verifica se uma senha em texto plano corresponde à senha criptografada.
     * 
     * @param plainPassword A senha em texto plano
     * @param encryptedPassword A senha criptografada
     * @return true se as senhas correspondem, false caso contrário
     */
    public boolean verifyPassword(String plainPassword, String encryptedPassword) {
        if (plainPassword == null || encryptedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, encryptedPassword);
    }

    /**
     * Verifica se uma senha já está criptografada baseado no padrão do BCrypt.
     * 
     * @param password A senha a ser verificada
     * @return true se a senha está criptografada, false caso contrário
     */
    public boolean isPasswordEncrypted(String password) {
        if (password == null) {
            return false;
        }
        // BCrypt hash sempre começa com $2a$, $2b$, $2x$ ou $2y$ seguido do custo e tem 60 caracteres
        return password.matches("^\\$2[abxy]\\$\\d{2}\\$.{53}$");
    }
}
