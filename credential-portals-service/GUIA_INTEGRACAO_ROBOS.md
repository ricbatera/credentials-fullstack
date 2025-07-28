# Guia de Integração para Robôs - API de Credenciais

Este documento fornece instruções detalhadas para robôs que desejam consumir a API de Credenciais, incluindo a implementação de chaves RSA e o processo de cadastro.

## 📋 Visão Geral

A API de Credenciais utiliza criptografia RSA para proteger as senhas retornadas. Cada robô consumidor deve:

1. Gerar um par de chaves RSA (pública e privada)
2. Registrar sua chave pública na API
3. Usar sua chave privada para descriptografar as senhas recebidas

## 🔐 Geração de Chaves RSA

### Método 1: Usando OpenSSL (Linha de Comando)

```bash
# Gerar chave privada RSA de 2048 bits
openssl genrsa -out private_key.pem 2048

# Extrair chave pública
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Visualizar chave pública (formato para cadastro na API)
openssl rsa -in private_key.pem -pubout -outform PEM
```

### Método 2: Usando Java

```java
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAKeyGenerator {
    
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
    
    public static String getPublicKeyPEM(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\\n");
        
        // Quebrar em linhas de 64 caracteres
        for (int i = 0; i < base64.length(); i += 64) {
            int end = Math.min(i + 64, base64.length());
            pem.append(base64.substring(i, end)).append("\\n");
        }
        
        pem.append("-----END PUBLIC KEY-----");
        return pem.toString();
    }
}
```

### Método 3: Usando Python

```python
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization

# Gerar chave privada
private_key = rsa.generate_private_key(
    public_exponent=65537,
    key_size=2048
)

# Obter chave pública
public_key = private_key.public_key()

# Serializar chave privada
private_pem = private_key.private_bytes(
    encoding=serialization.Encoding.PEM,
    format=serialization.PrivateFormat.PKCS8,
    encryption_algorithm=serialization.NoEncryption()
)

# Serializar chave pública (formato para cadastro na API)
public_pem = public_key.public_bytes(
    encoding=serialization.Encoding.PEM,
    format=serialization.PublicFormat.SubjectPublicKeyInfo
)

print("Chave Pública:")
print(public_pem.decode('utf-8'))
```

### Método 4: Usando C#

```csharp
using System;
using System.Security.Cryptography;
using System.Text;

public class RSAKeyGenerator
{
    public static (string publicKey, string privateKey) GenerateKeyPair()
    {
        using (var rsa = RSA.Create(2048))
        {
            // Exportar chave privada
            var privateKeyBytes = rsa.ExportRSAPrivateKey();
            var privateKeyPem = ConvertToPem(privateKeyBytes, "RSA PRIVATE KEY");
            
            // Exportar chave pública
            var publicKeyBytes = rsa.ExportSubjectPublicKeyInfo();
            var publicKeyPem = ConvertToPem(publicKeyBytes, "PUBLIC KEY");
            
            return (publicKeyPem, privateKeyPem);
        }
    }
    
    private static string ConvertToPem(byte[] keyBytes, string keyType)
    {
        var base64 = Convert.ToBase64String(keyBytes);
        var sb = new StringBuilder();
        
        sb.AppendLine($"-----BEGIN {keyType}-----");
        
        // Quebrar em linhas de 64 caracteres
        for (int i = 0; i < base64.Length; i += 64)
        {
            int length = Math.Min(64, base64.Length - i);
            sb.AppendLine(base64.Substring(i, length));
        }
        
        sb.AppendLine($"-----END {keyType}-----");
        return sb.ToString();
    }
}
```

## 📝 Cadastro na API

### 1. Registrar Chave Pública

**Endpoint:** `POST /api/consumer-keys`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
    "consumerIdentifier": "meu-robo-001",
    "consumerName": "Robô de Automação Shopping XYZ",
    "publicKey": "-----BEGIN PUBLIC KEY-----\\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\\n-----END PUBLIC KEY-----",
    "description": "Robô responsável pela automação de processos do Shopping XYZ",
    "contactEmail": "admin@shopping-xyz.com"
}
```

**Resposta de Sucesso (201):**
```json
{
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "consumerIdentifier": "meu-robo-001",
    "consumerName": "Robô de Automação Shopping XYZ",
    "isActive": true,
    "createdAt": "2025-07-26T10:30:00",
    "expiresAt": "2026-07-26T10:30:00"
}
```

### 2. Verificar Status da Chave

**Endpoint:** `GET /api/consumer-keys/consumer/{consumerIdentifier}/valid`

**Exemplo:**
```bash
curl -X GET "https://api.exemplo.com/api/consumer-keys/consumer/meu-robo-001/valid"
```

## 🔍 Consumindo Credenciais

### Endpoint para Obter Credenciais

**Endpoint:** `GET /api/consumer-keys/credentials`

**Headers:**
```
X-Consumer-Identifier: meu-robo-001
```

**Resposta:**
```json
[
    {
        "urlPortal": "https://portal.shopping-abc.com",
        "username": "admin_abc",
        "password": "ZXhhbXBsZSBlbmNyeXB0ZWQgcGFzc3dvcmQ=",
        "nameMall": "Shopping ABC"
    },
    {
        "urlPortal": "https://portal.shopping-xyz.com", 
        "username": "user_xyz",
        "password": "YW5vdGhlciBlbmNyeXB0ZWQgcGFzc3dvcmQ=",
        "nameMall": "Shopping XYZ"
    }
]
```

**⚠️ Importante:** O campo `password` retorna a senha criptografada com RSA usando sua chave pública.

## 🔓 Descriptografando Senhas

### Java

```java
import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class RSADecryption {
    
    public static String decryptPassword(String encryptedPassword, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        
        return new String(decryptedBytes, "UTF-8");
    }
    
    public static PrivateKey loadPrivateKey(String privateKeyPEM) throws Exception {
        String privateKeyContent = privateKeyPEM
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
            
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        return keyFactory.generatePrivate(spec);
    }
}
```

### Python

```python
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import padding
import base64

def decrypt_password(encrypted_password, private_key):
    # Decodificar base64
    encrypted_bytes = base64.b64decode(encrypted_password)
    
    # Descriptografar
    decrypted_bytes = private_key.decrypt(
        encrypted_bytes,
        padding.OAEP(
            mgf=padding.MGF1(algorithm=hashes.SHA256()),
            algorithm=hashes.SHA256(),
            label=None
        )
    )
    
    return decrypted_bytes.decode('utf-8')

def load_private_key(private_key_pem):
    return serialization.load_pem_private_key(
        private_key_pem.encode('utf-8'),
        password=None
    )
```

### C#

```csharp
using System;
using System.Security.Cryptography;
using System.Text;

public class RSADecryption
{
    public static string DecryptPassword(string encryptedPassword, RSA privateKey)
    {
        try
        {
            // Decodificar base64
            byte[] encryptedBytes = Convert.FromBase64String(encryptedPassword);
            
            // Descriptografar usando OAEP
            byte[] decryptedBytes = privateKey.Decrypt(encryptedBytes, RSAEncryptionPadding.OaepSHA256);
            
            return Encoding.UTF8.GetString(decryptedBytes);
        }
        catch (Exception ex)
        {
            throw new InvalidOperationException("Erro ao descriptografar senha", ex);
        }
    }
    
    public static RSA LoadPrivateKey(string privateKeyPem)
    {
        try
        {
            var rsa = RSA.Create();
            
            // Remover headers e footers PEM
            string privateKeyContent = privateKeyPem
                .Replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .Replace("-----END RSA PRIVATE KEY-----", "")
                .Replace("-----BEGIN PRIVATE KEY-----", "")
                .Replace("-----END PRIVATE KEY-----", "")
                .Replace("\n", "")
                .Replace("\r", "");
            
            byte[] keyBytes = Convert.FromBase64String(privateKeyContent);
            
            // Tentar importar como PKCS#8 primeiro, depois como RSA se falhar
            try
            {
                rsa.ImportPkcs8PrivateKey(keyBytes, out _);
            }
            catch
            {
                rsa.ImportRSAPrivateKey(keyBytes, out _);
            }
            
            return rsa;
        }
        catch (Exception ex)
        {
            throw new InvalidOperationException("Erro ao carregar chave privada", ex);
        }
    }
}
```

### Node.js

```javascript
const crypto = require('crypto');
const fs = require('fs');

function decryptPassword(encryptedPassword, privateKeyPath) {
    const privateKey = fs.readFileSync(privateKeyPath, 'utf8');
    const buffer = Buffer.from(encryptedPassword, 'base64');
    
    const decrypted = crypto.privateDecrypt(
        {
            key: privateKey,
            padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
            oaepHash: 'sha256'
        },
        buffer
    );
    
    return decrypted.toString('utf8');
}
```

## 🛡️ Boas Práticas de Segurança

### 1. Proteção da Chave Privada
- **NUNCA** compartilhe sua chave privada
- Armazene em local seguro com permissões restritas
- Use variáveis de ambiente ou cofres de segurança
- Considere usar HSM (Hardware Security Module) em produção

### 2. Rotação de Chaves
- Implemente rotação periódica de chaves (recomendado: a cada 12 meses)
- Monitore a data de expiração usando o endpoint de verificação
- Tenha um processo automatizado para renovação

### 3. Monitoramento
- Registre todas as chamadas à API
- Monitore tentativas de acesso não autorizadas
- Implemente alertas para falhas de descriptografia

## 🔄 Exemplo de Implementação Completa

### Java Spring Boot

```java
@Service
public class CredentialsConsumerService {
    
    @Value("${api.consumer.identifier}")
    private String consumerIdentifier;
    
    @Value("${api.consumer.private-key-path}")
    private String privateKeyPath;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public List<DecryptedCredential> getCredentials() {
        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Consumer-Identifier", consumerIdentifier);
        
        // Fazer requisição
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<BasicCredentialsResponseDTO[]> response = restTemplate.exchange(
            "/api/consumer-keys/credentials",
            HttpMethod.GET,
            entity,
            BasicCredentialsResponseDTO[].class
        );
        
        // Descriptografar senhas
        PrivateKey privateKey = loadPrivateKey();
        return Arrays.stream(response.getBody())
            .map(cred -> new DecryptedCredential(
                cred.getUrlPortal(),
                cred.getUsername(),
                decryptPassword(cred.getPassword(), privateKey),
                cred.getNameMall()
            ))
            .collect(Collectors.toList());
    }
}
```

### C# ASP.NET Core

```csharp
using Microsoft.Extensions.Configuration;
using System.Text.Json;

[ApiController]
[Route("api/[controller]")]
public class CredentialsConsumerController : ControllerBase
{
    private readonly HttpClient _httpClient;
    private readonly IConfiguration _configuration;
    private readonly RSA _privateKey;

    public CredentialsConsumerController(HttpClient httpClient, IConfiguration configuration)
    {
        _httpClient = httpClient;
        _configuration = configuration;
        
        // Carregar chave privada na inicialização
        string privateKeyPath = _configuration["Api:Consumer:PrivateKeyPath"];
        string privateKeyPem = File.ReadAllText(privateKeyPath);
        _privateKey = RSADecryption.LoadPrivateKey(privateKeyPem);
    }

    [HttpGet("credentials")]
    public async Task<ActionResult<List<DecryptedCredential>>> GetCredentials()
    {
        try
        {
            // Configurar headers
            string consumerIdentifier = _configuration["Api:Consumer:Identifier"];
            _httpClient.DefaultRequestHeaders.Add("X-Consumer-Identifier", consumerIdentifier);

            // Fazer requisição
            var response = await _httpClient.GetAsync("/api/consumer-keys/credentials");
            response.EnsureSuccessStatusCode();

            string jsonContent = await response.Content.ReadAsStringAsync();
            var basicCredentials = JsonSerializer.Deserialize<List<BasicCredentialsResponseDTO>>(jsonContent);

            // Descriptografar senhas
            var decryptedCredentials = basicCredentials.Select(cred => new DecryptedCredential
            {
                UrlPortal = cred.UrlPortal,
                Username = cred.Username,
                Password = RSADecryption.DecryptPassword(cred.Password, _privateKey),
                NameMall = cred.NameMall
            }).ToList();

            return Ok(decryptedCredentials);
        }
        catch (HttpRequestException ex)
        {
            return StatusCode(500, $"Erro na requisição: {ex.Message}");
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Erro interno: {ex.Message}");
        }
    }
}

public class BasicCredentialsResponseDTO
{
    public string UrlPortal { get; set; }
    public string Username { get; set; }
    public string Password { get; set; }
    public string NameMall { get; set; }
}

public class DecryptedCredential
{
    public string UrlPortal { get; set; }
    public string Username { get; set; }
    public string Password { get; set; }
    public string NameMall { get; set; }
}
```

## 📞 Suporte

Para dúvidas técnicas ou problemas de integração:

- **Email:** suporte-api@consultdg.com
- **Documentação Swagger:** `/swagger-ui.html`
- **Health Check:** `GET /actuator/health`

## 🔄 Versionamento

- **Versão Atual:** v1
- **Endpoint Base:** `/api/consumer-keys`
- **Compatibilidade:** Mantida por pelo menos 12 meses após nova versão

## 💻 Linguagens Suportadas

Este guia inclui exemplos completos de implementação nas seguintes linguagens:

- ☕ **Java** (Spring Boot) - Geração de chaves, descriptografia e consumo da API
- 🐍 **Python** (cryptography) - Implementação completa com bibliotecas padrão
- 🟢 **Node.js** (crypto nativo) - Usando módulos nativos do Node.js
- 🔷 **C#** (ASP.NET Core) - Implementação para .NET com System.Security.Cryptography
- 🖥️ **OpenSSL** (linha de comando) - Para geração rápida de chaves em qualquer sistema

Cada linguagem inclui:
- ✅ Geração de pares de chaves RSA
- ✅ Carregamento de chaves privadas
- ✅ Descriptografia de senhas
- ✅ Exemplos de consumo da API
- ✅ Tratamento de erros

---

**⚠️ Lembre-se:** Este sistema lida com credenciais sensíveis. Sempre siga as melhores práticas de segurança e mantenha suas chaves privadas protegidas!
