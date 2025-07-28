package br.com.consultdg.credential_portals_service.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Serviço responsável pela criptografia/descriptografia de dados usando RSA.
 * Utilizado para criptografar senhas que serão enviadas para consumidores da API.
 */
@Service
public class RSAEncryptionService {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 2048;

    /**
     * Gera um par de chaves RSA (pública e privada).
     * 
     * @return KeyPair contendo chave pública e privada
     * @throws Exception se houver erro na geração das chaves
     */
    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGenerator.initialize(KEY_SIZE);
        return keyGenerator.generateKeyPair();
    }

    /**
     * Converte uma chave pública para string Base64.
     * 
     * @param publicKey A chave pública
     * @return String Base64 da chave pública
     */
    public String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Converte uma chave privada para string Base64.
     * 
     * @param privateKey A chave privada
     * @return String Base64 da chave privada
     */
    public String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Converte uma string Base64 ou PEM em chave pública.
     * 
     * @param publicKeyString String Base64 ou formato PEM da chave pública
     * @return PublicKey
     * @throws Exception se houver erro na conversão
     */
    public PublicKey stringToPublicKey(String publicKeyString) throws Exception {
        // Remove headers PEM se presentes e quebras de linha
        String cleanKey = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", ""); // Remove todos os espaços, quebras de linha, etc.
        
        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    /**
     * Converte uma string Base64 ou PEM em chave privada.
     * 
     * @param privateKeyString String Base64 ou formato PEM da chave privada
     * @return PrivateKey
     * @throws Exception se houver erro na conversão
     */
    public PrivateKey stringToPrivateKey(String privateKeyString) throws Exception {
        // Remove headers PEM se presentes e quebras de linha
        String cleanKey = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // Remove todos os espaços, quebras de linha, etc.
        
        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Criptografa dados usando a chave pública.
     * 
     * @param data Os dados a serem criptografados
     * @param publicKey A chave pública para criptografia
     * @return String Base64 dos dados criptografados
     * @throws Exception se houver erro na criptografia
     */
    public String encrypt(String data, PublicKey publicKey) throws Exception {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Dados para criptografia não podem ser nulos ou vazios");
        }
        
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Criptografa dados usando a chave pública em formato string.
     * 
     * @param data Os dados a serem criptografados
     * @param publicKeyString A chave pública em formato Base64
     * @return String Base64 dos dados criptografados
     * @throws Exception se houver erro na criptografia
     */
    public String encrypt(String data, String publicKeyString) throws Exception {
        PublicKey publicKey = stringToPublicKey(publicKeyString);
        return encrypt(data, publicKey);
    }

    /**
     * Descriptografa dados usando a chave privada.
     * 
     * @param encryptedData Os dados criptografados em Base64
     * @param privateKey A chave privada para descriptografia
     * @return String com os dados descriptografados
     * @throws Exception se houver erro na descriptografia
     */
    public String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            throw new IllegalArgumentException("Dados criptografados não podem ser nulos ou vazios");
        }
        
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData, "UTF-8");
    }

    /**
     * Descriptografa dados usando a chave privada em formato string.
     * 
     * @param encryptedData Os dados criptografados em Base64
     * @param privateKeyString A chave privada em formato Base64
     * @return String com os dados descriptografados
     * @throws Exception se houver erro na descriptografia
     */
    public String decrypt(String encryptedData, String privateKeyString) throws Exception {
        PrivateKey privateKey = stringToPrivateKey(privateKeyString);
        return decrypt(encryptedData, privateKey);
    }

    /**
     * Valida se uma chave pública é válida.
     * 
     * @param publicKeyString A chave pública em formato Base64
     * @return true se a chave é válida, false caso contrário
     */
    public boolean isValidPublicKey(String publicKeyString) {
        try {
            stringToPublicKey(publicKeyString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
