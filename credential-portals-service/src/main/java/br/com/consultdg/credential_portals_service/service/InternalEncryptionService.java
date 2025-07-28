package br.com.consultdg.credential_portals_service.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Serviço responsável pela criptografia simétrica interna.
 * Usado para armazenar senhas de forma reversível para posterior criptografia com RSA dos consumidores.
 */
@Service
public class InternalEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    
    // Chave fixa para demonstração - em produção, deve ser gerenciada de forma segura
    private static final String INTERNAL_KEY = "MySecretKey12345"; // 16 bytes para AES-128

    /**
     * Criptografa uma senha usando AES.
     * 
     * @param plainPassword A senha em texto plano
     * @return A senha criptografada em Base64
     */
    public String encrypt(String plainPassword) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(INTERNAL_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar senha internamente: " + e.getMessage(), e);
        }
    }

    /**
     * Descriptografa uma senha usando AES.
     * 
     * @param encryptedPassword A senha criptografada em Base64
     * @return A senha em texto plano
     */
    public String decrypt(String encryptedPassword) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(INTERNAL_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar senha internamente: " + e.getMessage(), e);
        }
    }
}
