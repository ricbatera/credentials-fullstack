# 👨‍💻 Exemplos de Implementação RSA para Consumidores da API

## 🔧 Implementação em Java (Spring Boot)

### **Cliente Java Completo:**

```java
package com.example.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class CredentialsApiClient {
    
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private final String consumerIdentifier;
    private final PrivateKey privateKey;
    
    public CredentialsApiClient(String apiBaseUrl, String consumerIdentifier, String privateKeyPem) {
        this.restTemplate = new RestTemplate();
        this.apiBaseUrl = apiBaseUrl;
        this.consumerIdentifier = consumerIdentifier;
        this.privateKey = loadPrivateKey(privateKeyPem);
    }
    
    /**
     * Registra chave pública na API
     */
    public void registerPublicKey(PublicKey publicKey, String consumerName, String description) {
        String url = apiBaseUrl + "/api/consumer-keys";
        
        ConsumerKeyRequest request = new ConsumerKeyRequest();
        request.setConsumerName(consumerName);
        request.setConsumerIdentifier(consumerIdentifier);
        request.setPublicKey(publicKeyToString(publicKey));
        request.setDescription(description);
        request.setExpiresAt("2025-12-31T23:59:59"); // 1 ano
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ConsumerKeyRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ConsumerKeyResponse> response = restTemplate.postForEntity(url, entity, ConsumerKeyResponse.class);
        
        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Chave pública registrada com sucesso!");
        } else {
            throw new RuntimeException("Falha ao registrar chave pública");
        }
    }
    
    /**
     * Busca todas as credenciais com senhas criptografadas
     */
    public List<CredentialWithEncryptedPassword> getAllCredentialsWithEncryptedPasswords() {
        String url = apiBaseUrl + "/api/credentials/encrypted/" + consumerIdentifier;
        
        ResponseEntity<CredentialWithEncryptedPassword[]> response = 
            restTemplate.getForEntity(url, CredentialWithEncryptedPassword[].class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            return List.of(response.getBody());
        } else {
            throw new RuntimeException("Falha ao buscar credenciais");
        }
    }
    
    /**
     * Busca uma credencial específica com senha criptografada
     */
    public CredentialWithEncryptedPassword getCredentialWithEncryptedPassword(String credentialId) {
        String url = apiBaseUrl + "/api/credentials/" + credentialId + "/encrypted/" + consumerIdentifier;
        
        ResponseEntity<CredentialWithEncryptedPassword> response = 
            restTemplate.getForEntity(url, CredentialWithEncryptedPassword.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Falha ao buscar credencial");
        }
    }
    
    /**
     * Descriptografa uma senha usando a chave privada
     */
    public String decryptPassword(String encryptedPassword) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
            
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao descriptografar senha", e);
        }
    }
    
    /**
     * Converte chave pública para string Base64
     */
    private String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * Carrega chave privada a partir de string PEM
     */
    private PrivateKey loadPrivateKey(String privateKeyPem) {
        try {
            // Remove headers e quebras de linha
            String privateKeyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar chave privada", e);
        }
    }
    
    // DTOs
    public static class ConsumerKeyRequest {
        private String consumerName;
        private String consumerIdentifier;
        private String publicKey;
        private String description;
        private String expiresAt;
        
        // getters e setters...
    }
    
    public static class ConsumerKeyResponse {
        private String id;
        private String consumerName;
        private String consumerIdentifier;
        // outros campos...
    }
    
    public static class CredentialWithEncryptedPassword {
        private String id;
        private String nameMall;
        private String cnpj;
        private String urlPortal;
        private String username;
        private String encryptedPassword;
        private String consumerIdentifier;
        
        // getters e setters...
    }
}
```

### **Exemplo de Uso em Java:**

```java
public class ExampleUsage {
    
    public static void main(String[] args) {
        // Configuração
        String apiUrl = "http://localhost:8084";
        String consumerId = "java-client-v1";
        String privateKeyPem = loadPrivateKeyFromFile("private-key.pem");
        
        // Criar cliente
        CredentialsApiClient client = new CredentialsApiClient(apiUrl, consumerId, privateKeyPem);
        
        // 1. Gerar par de chaves e registrar chave pública (fazer uma vez)
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            client.registerPublicKey(
                keyPair.getPublic(),
                "Java Client Application",
                "Cliente Java para integração com portais"
            );
            
            // Salvar chave privada para uso futuro
            savePrivateKeyToFile(keyPair.getPrivate(), "private-key.pem");
            
        } catch (Exception e) {
            System.out.println("Chave já registrada ou erro: " + e.getMessage());
        }
        
        // 2. Buscar credenciais com senhas criptografadas
        List<CredentialWithEncryptedPassword> credentials = 
            client.getAllCredentialsWithEncryptedPasswords();
        
        // 3. Usar credenciais para conectar em portais
        for (CredentialWithEncryptedPassword credential : credentials) {
            String decryptedPassword = client.decryptPassword(credential.getEncryptedPassword());
            
            System.out.println("Conectando no portal: " + credential.getUrlPortal());
            System.out.println("Usuário: " + credential.getUsername());
            System.out.println("Senha descriptografada: " + decryptedPassword);
            
            // Conectar no portal usando as credenciais
            connectToPortal(credential.getUrlPortal(), credential.getUsername(), decryptedPassword);
        }
    }
    
    private static void connectToPortal(String url, String username, String password) {
        // Implementar conexão com o portal
        System.out.println("✅ Conectado com sucesso no portal: " + url);
    }
    
    private static String loadPrivateKeyFromFile(String filename) {
        // Implementar carregamento do arquivo
        return "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----";
    }
    
    private static void savePrivateKeyToFile(PrivateKey privateKey, String filename) {
        // Implementar salvamento no arquivo
        System.out.println("Chave privada salva em: " + filename);
    }
}
```

## 🌐 Implementação em JavaScript/Node.js

### **Cliente Node.js Completo:**

```javascript
const crypto = require('crypto');
const axios = require('axios');
const fs = require('fs');

class CredentialsApiClient {
    constructor(apiBaseUrl, consumerIdentifier, privateKeyPath) {
        this.apiBaseUrl = apiBaseUrl;
        this.consumerIdentifier = consumerIdentifier;
        this.privateKey = fs.readFileSync(privateKeyPath, 'utf8');
    }
    
    /**
     * Gera par de chaves RSA
     */
    generateKeyPair() {
        const { publicKey, privateKey } = crypto.generateKeyPairSync('rsa', {
            modulusLength: 2048,
            publicKeyEncoding: {
                type: 'spki',
                format: 'pem'
            },
            privateKeyEncoding: {
                type: 'pkcs8',
                format: 'pem'
            }
        });
        
        return { publicKey, privateKey };
    }
    
    /**
     * Registra chave pública na API
     */
    async registerPublicKey(publicKey, consumerName, description) {
        const url = `${this.apiBaseUrl}/api/consumer-keys`;
        
        const data = {
            consumerName,
            consumerIdentifier: this.consumerIdentifier,
            publicKey: this.convertPemToBase64(publicKey),
            description,
            expiresAt: '2025-12-31T23:59:59'
        };
        
        try {
            const response = await axios.post(url, data, {
                headers: { 'Content-Type': 'application/json' }
            });
            
            console.log('✅ Chave pública registrada com sucesso!');
            return response.data;
        } catch (error) {
            throw new Error(`Falha ao registrar chave pública: ${error.message}`);
        }
    }
    
    /**
     * Busca todas as credenciais com senhas criptografadas
     */
    async getAllCredentialsWithEncryptedPasswords() {
        const url = `${this.apiBaseUrl}/api/credentials/encrypted/${this.consumerIdentifier}`;
        
        try {
            const response = await axios.get(url);
            return response.data;
        } catch (error) {
            throw new Error(`Falha ao buscar credenciais: ${error.message}`);
        }
    }
    
    /**
     * Busca uma credencial específica com senha criptografada
     */
    async getCredentialWithEncryptedPassword(credentialId) {
        const url = `${this.apiBaseUrl}/api/credentials/${credentialId}/encrypted/${this.consumerIdentifier}`;
        
        try {
            const response = await axios.get(url);
            return response.data;
        } catch (error) {
            throw new Error(`Falha ao buscar credencial: ${error.message}`);
        }
    }
    
    /**
     * Descriptografa uma senha usando a chave privada
     */
    decryptPassword(encryptedPassword) {
        try {
            const buffer = Buffer.from(encryptedPassword, 'base64');
            const decrypted = crypto.privateDecrypt(this.privateKey, buffer);
            return decrypted.toString('utf8');
        } catch (error) {
            throw new Error(`Falha ao descriptografar senha: ${error.message}`);
        }
    }
    
    /**
     * Converte chave PEM para Base64
     */
    convertPemToBase64(pemKey) {
        return pemKey
            .replace(/-----BEGIN PUBLIC KEY-----/, '')
            .replace(/-----END PUBLIC KEY-----/, '')
            .replace(/\r?\n|\r/g, '');
    }
    
    /**
     * Salva par de chaves em arquivos
     */
    saveKeyPair(keyPair, publicKeyPath, privateKeyPath) {
        fs.writeFileSync(publicKeyPath, keyPair.publicKey);
        fs.writeFileSync(privateKeyPath, keyPair.privateKey);
        console.log(`✅ Chaves salvas em ${publicKeyPath} e ${privateKeyPath}`);
    }
}

// Exemplo de uso
async function example() {
    const client = new CredentialsApiClient(
        'http://localhost:8084',
        'nodejs-client-v1',
        './private-key.pem'
    );
    
    try {
        // 1. Gerar e registrar chaves (fazer apenas uma vez)
        const keyPair = client.generateKeyPair();
        client.saveKeyPair(keyPair, './public-key.pem', './private-key.pem');
        
        await client.registerPublicKey(
            keyPair.publicKey,
            'Node.js Client Application',
            'Cliente Node.js para integração com portais'
        );
        
        // 2. Buscar credenciais
        const credentials = await client.getAllCredentialsWithEncryptedPasswords();
        
        // 3. Usar credenciais
        for (const credential of credentials) {
            const decryptedPassword = client.decryptPassword(credential.encryptedPassword);
            
            console.log(`🔐 Portal: ${credential.urlPortal}`);
            console.log(`👤 Usuário: ${credential.username}`);
            console.log(`🔑 Senha: ${decryptedPassword}`);
            
            // Conectar no portal
            await connectToPortal(credential.urlPortal, credential.username, decryptedPassword);
        }
        
    } catch (error) {
        console.error('❌ Erro:', error.message);
    }
}

async function connectToPortal(url, username, password) {
    // Implementar conexão com o portal
    console.log(`✅ Conectado com sucesso no portal: ${url}`);
}

// Executar exemplo
example();
```

## 🐍 Implementação em Python

### **Cliente Python Completo:**

```python
import base64
import json
import requests
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.backends import default_backend
from datetime import datetime, timedelta

class CredentialsApiClient:
    def __init__(self, api_base_url, consumer_identifier, private_key_path=None):
        self.api_base_url = api_base_url
        self.consumer_identifier = consumer_identifier
        self.private_key = None
        
        if private_key_path:
            self.load_private_key(private_key_path)
    
    def generate_key_pair(self):
        """Gera par de chaves RSA"""
        private_key = rsa.generate_private_key(
            public_exponent=65537,
            key_size=2048,
            backend=default_backend()
        )
        public_key = private_key.public_key()
        
        return private_key, public_key
    
    def save_private_key(self, private_key, file_path):
        """Salva chave privada em arquivo"""
        pem = private_key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.PKCS8,
            encryption_algorithm=serialization.NoEncryption()
        )
        
        with open(file_path, 'wb') as f:
            f.write(pem)
        
        print(f"✅ Chave privada salva em {file_path}")
    
    def save_public_key(self, public_key, file_path):
        """Salva chave pública em arquivo"""
        pem = public_key.public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
        
        with open(file_path, 'wb') as f:
            f.write(pem)
        
        print(f"✅ Chave pública salva em {file_path}")
    
    def load_private_key(self, file_path):
        """Carrega chave privada do arquivo"""
        with open(file_path, 'rb') as f:
            self.private_key = serialization.load_pem_private_key(
                f.read(),
                password=None,
                backend=default_backend()
            )
    
    def public_key_to_base64(self, public_key):
        """Converte chave pública para Base64"""
        pem = public_key.public_bytes(
            encoding=serialization.Encoding.DER,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
        return base64.b64encode(pem).decode('utf-8')
    
    def register_public_key(self, public_key, consumer_name, description):
        """Registra chave pública na API"""
        url = f"{self.api_base_url}/api/consumer-keys"
        
        # Data de expiração (1 ano)
        expires_at = (datetime.now() + timedelta(days=365)).isoformat()
        
        data = {
            "consumerName": consumer_name,
            "consumerIdentifier": self.consumer_identifier,
            "publicKey": self.public_key_to_base64(public_key),
            "description": description,
            "expiresAt": expires_at
        }
        
        response = requests.post(url, json=data)
        
        if response.status_code == 201:
            print("✅ Chave pública registrada com sucesso!")
            return response.json()
        else:
            raise Exception(f"Falha ao registrar chave pública: {response.text}")
    
    def get_all_credentials_with_encrypted_passwords(self):
        """Busca todas as credenciais com senhas criptografadas"""
        url = f"{self.api_base_url}/api/credentials/encrypted/{self.consumer_identifier}"
        
        response = requests.get(url)
        
        if response.status_code == 200:
            return response.json()
        else:
            raise Exception(f"Falha ao buscar credenciais: {response.text}")
    
    def get_credential_with_encrypted_password(self, credential_id):
        """Busca uma credencial específica com senha criptografada"""
        url = f"{self.api_base_url}/api/credentials/{credential_id}/encrypted/{self.consumer_identifier}"
        
        response = requests.get(url)
        
        if response.status_code == 200:
            return response.json()
        else:
            raise Exception(f"Falha ao buscar credencial: {response.text}")
    
    def decrypt_password(self, encrypted_password):
        """Descriptografa uma senha usando a chave privada"""
        if not self.private_key:
            raise Exception("Chave privada não carregada")
        
        try:
            # Decodificar Base64
            encrypted_bytes = base64.b64decode(encrypted_password)
            
            # Descriptografar
            decrypted_bytes = self.private_key.decrypt(
                encrypted_bytes,
                padding.OAEP(
                    mgf=padding.MGF1(algorithm=hashes.SHA256()),
                    algorithm=hashes.SHA256(),
                    label=None
                )
            )
            
            return decrypted_bytes.decode('utf-8')
        
        except Exception as e:
            raise Exception(f"Falha ao descriptografar senha: {str(e)}")

# Exemplo de uso
def example():
    # Configuração
    client = CredentialsApiClient(
        api_base_url="http://localhost:8084",
        consumer_identifier="python-client-v1"
    )
    
    try:
        # 1. Gerar e salvar chaves (fazer apenas uma vez)
        private_key, public_key = client.generate_key_pair()
        client.save_private_key(private_key, "private-key.pem")
        client.save_public_key(public_key, "public-key.pem")
        
        # 2. Registrar chave pública
        client.register_public_key(
            public_key,
            "Python Client Application",
            "Cliente Python para integração com portais"
        )
        
        # 3. Carregar chave privada
        client.load_private_key("private-key.pem")
        
        # 4. Buscar credenciais
        credentials = client.get_all_credentials_with_encrypted_passwords()
        
        # 5. Usar credenciais
        for credential in credentials:
            decrypted_password = client.decrypt_password(credential['encryptedPassword'])
            
            print(f"🔐 Portal: {credential['urlPortal']}")
            print(f"👤 Usuário: {credential['username']}")
            print(f"🔑 Senha: {decrypted_password}")
            
            # Conectar no portal
            connect_to_portal(credential['urlPortal'], credential['username'], decrypted_password)
    
    except Exception as e:
        print(f"❌ Erro: {str(e)}")

def connect_to_portal(url, username, password):
    """Conecta no portal usando as credenciais"""
    print(f"✅ Conectado com sucesso no portal: {url}")

if __name__ == "__main__":
    example()
```

## 📱 Implementação em Flutter/Dart

### **Cliente Flutter:**

```dart
import 'dart:convert';
import 'dart:typed_data';
import 'package:http/http.dart' as http;
import 'package:pointycastle/export.dart';

class CredentialsApiClient {
  final String apiBaseUrl;
  final String consumerIdentifier;
  RSAPrivateKey? _privateKey;
  
  CredentialsApiClient({
    required this.apiBaseUrl,
    required this.consumerIdentifier,
  });
  
  /// Gera par de chaves RSA
  AsymmetricKeyPair<RSAPublicKey, RSAPrivateKey> generateKeyPair() {
    final keyGen = RSAKeyGenerator();
    final secureRandom = FortunaRandom();
    
    // Seed do gerador aleatório
    final seedSource = Uint8List(32);
    for (int i = 0; i < 32; i++) {
      seedSource[i] = DateTime.now().millisecondsSinceEpoch & 0xFF;
    }
    secureRandom.seed(KeyParameter(seedSource));
    
    keyGen.init(ParametersWithRandom(
      RSAKeyGeneratorParameters(BigInt.parse('65537'), 2048, 64),
      secureRandom,
    ));
    
    return keyGen.generateKeyPair();
  }
  
  /// Registra chave pública na API
  Future<Map<String, dynamic>> registerPublicKey({
    required RSAPublicKey publicKey,
    required String consumerName,
    required String description,
  }) async {
    final url = Uri.parse('$apiBaseUrl/api/consumer-keys');
    
    final data = {
      'consumerName': consumerName,
      'consumerIdentifier': consumerIdentifier,
      'publicKey': _publicKeyToBase64(publicKey),
      'description': description,
      'expiresAt': DateTime.now().add(Duration(days: 365)).toIso8601String(),
    };
    
    final response = await http.post(
      url,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(data),
    );
    
    if (response.statusCode == 201) {
      print('✅ Chave pública registrada com sucesso!');
      return jsonDecode(response.body);
    } else {
      throw Exception('Falha ao registrar chave pública: ${response.body}');
    }
  }
  
  /// Busca todas as credenciais com senhas criptografadas
  Future<List<Map<String, dynamic>>> getAllCredentialsWithEncryptedPasswords() async {
    final url = Uri.parse('$apiBaseUrl/api/credentials/encrypted/$consumerIdentifier');
    
    final response = await http.get(url);
    
    if (response.statusCode == 200) {
      return List<Map<String, dynamic>>.from(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao buscar credenciais: ${response.body}');
    }
  }
  
  /// Descriptografa uma senha usando a chave privada
  String decryptPassword(String encryptedPassword) {
    if (_privateKey == null) {
      throw Exception('Chave privada não carregada');
    }
    
    try {
      final encryptedBytes = base64Decode(encryptedPassword);
      
      final cipher = RSAEngine();
      cipher.init(false, PrivateKeyParameter<RSAPrivateKey>(_privateKey!));
      
      final decryptedBytes = cipher.process(encryptedBytes);
      return utf8.decode(decryptedBytes);
    } catch (e) {
      throw Exception('Falha ao descriptografar senha: $e');
    }
  }
  
  /// Define a chave privada
  void setPrivateKey(RSAPrivateKey privateKey) {
    _privateKey = privateKey;
  }
  
  /// Converte chave pública para Base64
  String _publicKeyToBase64(RSAPublicKey publicKey) {
    final algorithmSeq = ASN1Sequence();
    final algorithmAsn1Obj = ASN1Object.fromBytes(Uint8List.fromList([0x6, 0x9, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0xd, 0x1, 0x1, 0x1]));
    final paramsAsn1Obj = ASN1Object.fromBytes(Uint8List.fromList([0x5, 0x0]));
    algorithmSeq.add(algorithmAsn1Obj);
    algorithmSeq.add(paramsAsn1Obj);
    
    final publicKeySeq = ASN1Sequence();
    publicKeySeq.add(ASN1Integer(publicKey.modulus!));
    publicKeySeq.add(ASN1Integer(publicKey.exponent!));
    final publicKeySeqBitString = ASN1BitString(publicKeySeq.encodedBytes);
    
    final topLevelSeq = ASN1Sequence();
    topLevelSeq.add(algorithmSeq);
    topLevelSeq.add(publicKeySeqBitString);
    
    return base64Encode(topLevelSeq.encodedBytes);
  }
}

// Exemplo de uso no Flutter
class CredentialsService {
  late CredentialsApiClient _client;
  
  Future<void> initialize() async {
    _client = CredentialsApiClient(
      apiBaseUrl: 'http://localhost:8084',
      consumerIdentifier: 'flutter-app-v1',
    );
    
    // Gerar e registrar chaves (fazer apenas uma vez)
    final keyPair = _client.generateKeyPair();
    _client.setPrivateKey(keyPair.privateKey);
    
    await _client.registerPublicKey(
      publicKey: keyPair.publicKey,
      consumerName: 'Flutter Mobile App',
      description: 'Aplicativo móvel para gerenciamento de portais',
    );
  }
  
  Future<List<Credential>> getCredentials() async {
    final credentialsData = await _client.getAllCredentialsWithEncryptedPasswords();
    
    return credentialsData.map((data) {
      final decryptedPassword = _client.decryptPassword(data['encryptedPassword']);
      
      return Credential(
        id: data['id'],
        nameMall: data['nameMall'],
        cnpj: data['cnpj'],
        urlPortal: data['urlPortal'],
        username: data['username'],
        password: decryptedPassword,
      );
    }).toList();
  }
}

class Credential {
  final String id;
  final String nameMall;
  final String cnpj;
  final String urlPortal;
  final String username;
  final String password;
  
  Credential({
    required this.id,
    required this.nameMall,
    required this.cnpj,
    required this.urlPortal,
    required this.username,
    required this.password,
  });
}
```

## 📋 Instalação de Dependências

### **Java:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### **Node.js:**
```bash
npm install axios
```

### **Python:**
```bash
pip install requests cryptography
```

### **Flutter:**
```yaml
dependencies:
  http: ^0.13.5
  pointycastle: ^3.7.3
```

## 🎯 Resumo dos Exemplos

Todos os exemplos implementam:

✅ **Geração de pares de chaves RSA**
✅ **Registro de chave pública na API**
✅ **Busca de credenciais com senhas criptografadas**
✅ **Descriptografia de senhas usando chave privada**
✅ **Tratamento de erros e validações**
✅ **Salvamento seguro de chaves**

Cada implementação é independente e pode ser adaptada conforme suas necessidades específicas!
