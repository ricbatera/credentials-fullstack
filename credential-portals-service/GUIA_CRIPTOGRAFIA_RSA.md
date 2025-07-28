# 🔐 Guia de Implementação: Criptografia RSA para Consumidores da API

## 📋 Visão Geral da Solução

A solução implementada permite que consumidores autorizados da API recebam senhas criptografadas usando **criptografia assimétrica RSA**. Cada consumidor possui um par de chaves (pública/privada), onde:

- **Chave Pública**: Registrada na API, usada para criptografar senhas
- **Chave Privada**: Mantida pelo consumidor, usada para descriptografar senhas

## 🏗️ Arquitetura da Solução

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Consumidor    │    │   API Service   │    │   Banco de      │
│   (Frontend)    │    │                 │    │   Dados         │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Chave Privada   │    │ Chave Pública   │    │ Senhas BCrypt   │
│ (Descriptografa)│◄──►│ (Criptografa)   │◄──►│ (Armazenadas)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔧 Componentes Implementados

### 1. **RSAEncryptionService**
- Geração de pares de chaves RSA
- Criptografia/descriptografia de dados
- Conversão de chaves para Base64
- Validação de chaves públicas

### 2. **ConsumerPublicKey** (Entidade)
- Armazena chaves públicas dos consumidores
- Controle de expiração
- Identificação única por consumidor

### 3. **Novos Endpoints da API**

#### **Gerenciamento de Chaves Públicas:**
- `GET /api/consumer-keys` - Lista todas as chaves
- `POST /api/consumer-keys` - Registra nova chave
- `GET /api/consumer-keys/consumer/{id}` - Busca chave por consumidor
- `DELETE /api/consumer-keys/{id}` - Remove chave

#### **Credenciais com Senhas Criptografadas:**
- `GET /api/credentials/encrypted/{consumerId}` - Lista todas com senhas criptografadas
- `GET /api/credentials/{id}/encrypted/{consumerId}` - Busca uma com senha criptografada
- `POST /api/credentials/encrypt-password/{consumerId}` - Criptografa senha específica

## 🚀 Como Usar - Passo a Passo

### **Passo 1: Gerar Par de Chaves RSA**

#### Opção A: Usar endpoint da API
```bash
curl -X GET "http://localhost:8084/api/consumer-keys/generate-example"
```

#### Opção B: Gerar manualmente (Java)
```java
RSAEncryptionService rsaService = new RSAEncryptionService();
KeyPair keyPair = rsaService.generateKeyPair();
String publicKey = rsaService.publicKeyToString(keyPair.getPublic());
String privateKey = rsaService.privateKeyToString(keyPair.getPrivate());
```

### **Passo 2: Registrar Chave Pública na API**

```bash
curl -X POST "http://localhost:8084/api/consumer-keys" \
     -H "Content-Type: application/json" \
     -d '{
       "consumerName": "Frontend Shopping App",
       "consumerIdentifier": "frontend-app-v1",
       "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...",
       "description": "Chave para aplicação frontend do shopping",
       "expiresAt": "2025-12-31T23:59:59"
     }'
```

### **Passo 3: Solicitar Credenciais com Senhas Criptografadas**

#### Buscar todas as credenciais:
```bash
curl -X GET "http://localhost:8084/api/credentials/encrypted/frontend-app-v1"
```

#### Buscar credencial específica:
```bash
curl -X GET "http://localhost:8084/api/credentials/123e4567-e89b-12d3-a456-426614174000/encrypted/frontend-app-v1"
```

### **Passo 4: Descriptografar Senhas no Cliente**

#### Exemplo em Java:
```java
RSAEncryptionService rsaService = new RSAEncryptionService();
String privateKeyString = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC...";
String encryptedPassword = "Base64EncryptedPasswordFromAPI";

String decryptedPassword = rsaService.decrypt(encryptedPassword, privateKeyString);
System.out.println("Senha descriptografada: " + decryptedPassword);
```

#### Exemplo em JavaScript (Node.js):
```javascript
const crypto = require('crypto');
const fs = require('fs');

// Carregar chave privada
const privateKey = fs.readFileSync('private-key.pem', 'utf8');

// Descriptografar senha
function decryptPassword(encryptedPassword) {
    const buffer = Buffer.from(encryptedPassword, 'base64');
    const decrypted = crypto.privateDecrypt(privateKey, buffer);
    return decrypted.toString('utf8');
}

const decryptedPassword = decryptPassword(encryptedPasswordFromAPI);
console.log('Senha descriptografada:', decryptedPassword);
```

## 🛡️ Segurança Implementada

### **Multicamadas de Proteção:**
1. **BCrypt** - Senhas armazenadas no banco com hash irreversível
2. **RSA 2048 bits** - Criptografia para transmissão
3. **Controle de Acesso** - Apenas consumidores registrados
4. **Expiração de Chaves** - Chaves podem ter data de expiração
5. **Identificação Única** - Cada consumidor tem identificador único

### **Fluxo de Segurança:**
```
1. Senha Original → BCrypt → Banco de Dados ✓
2. Consumidor Registra Chave Pública → API ✓
3. API Criptografa com Chave Pública RSA → Consumidor ✓
4. Consumidor Descriptografa com Chave Privada ✓
```

## 📊 Exemplos de Respostas da API

### **Credencial com Senha Criptografada:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nameMall": "Shopping Center ABC",
  "cnpj": "12345678901234",
  "urlPortal": "https://portal.shopping.com",
  "username": "admin",
  "encryptedPassword": "Base64EncryptedPasswordString==",
  "consumerIdentifier": "frontend-app-v1",
  "encryptionAlgorithm": "RSA",
  "createdAt": "2025-07-26T10:30:00",
  "updatedAt": "2025-07-26T15:45:00",
  "deletedAt": null,
  "active": true
}
```

### **Lista de Chaves Públicas:**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174000",
    "consumerName": "Frontend Shopping App",
    "consumerIdentifier": "frontend-app-v1",
    "keyAlgorithm": "RSA",
    "keySize": 2048,
    "createdAt": "2025-07-26T09:00:00",
    "updatedAt": "2025-07-26T09:00:00",
    "expiresAt": "2025-12-31T23:59:59",
    "active": true,
    "description": "Chave para aplicação frontend",
    "isValid": true
  }
]
```

## 🔍 Casos de Uso Práticos

### **Caso 1: Aplicação Frontend**
```javascript
// 1. Registrar chave pública uma vez
await registerPublicKey({
  consumerName: "React Shopping App",
  consumerIdentifier: "react-app-prod",
  publicKey: publicKeyBase64
});

// 2. Buscar credenciais com senhas criptografadas
const credentials = await fetch('/api/credentials/encrypted/react-app-prod');

// 3. Descriptografar senhas conforme necessário
credentials.forEach(cred => {
  const password = decryptPassword(cred.encryptedPassword);
  // Usar senha para autenticação em portais
});
```

### **Caso 2: Serviço de Integração**
```java
// Serviço que conecta em múltiplos portais
public class PortalIntegrationService {
    
    public void connectToPortals() {
        // Buscar credenciais criptografadas
        List<CredentialsWithEncryptedPasswordDTO> credentials = 
            credentialsService.findAllWithEncryptedPassword("integration-service-v1");
        
        for (var credential : credentials) {
            // Descriptografar senha
            String password = rsaService.decrypt(
                credential.getEncryptedPassword(), 
                privateKey
            );
            
            // Conectar no portal
            connectToPortal(credential.getUrlPortal(), 
                          credential.getUsername(), 
                          password);
        }
    }
}
```

### **Caso 3: Aplicação Mobile**
```swift
// iOS Swift
func getCredentialsWithEncryptedPasswords() {
    let url = URL(string: "http://api.example.com/credentials/encrypted/mobile-app-ios")!
    
    URLSession.shared.dataTask(with: url) { data, response, error in
        if let data = data {
            let credentials = try JSONDecoder().decode([CredentialWithEncryptedPassword].self, from: data)
            
            for credential in credentials {
                let decryptedPassword = decryptWithPrivateKey(credential.encryptedPassword)
                // Usar senha descriptografada
            }
        }
    }.resume()
}
```

## ⚠️ Considerações Importantes

### **Limitações do RSA:**
- **Tamanho máximo**: RSA 2048 bits pode criptografar ~245 bytes
- **Performance**: Mais lento que criptografia simétrica
- **Recomendação**: Ideal para senhas (dados pequenos)

### **Gerenciamento de Chaves:**
- **Chave Privada**: NUNCA compartilhar ou enviar para API
- **Rotação**: Recomendado renovar chaves periodicamente
- **Backup**: Manter backup seguro da chave privada
- **Expiração**: Configurar data de expiração adequada

### **Boas Práticas:**
1. **Validar chaves** antes de registrar
2. **Monitorar expiração** das chaves
3. **Log de acesso** para auditoria
4. **Rate limiting** para endpoints sensíveis
5. **HTTPS obrigatório** em produção

## 🧪 Testando a Implementação

### **Executar Testes:**
```bash
mvn test -Dtest="RSAEncryptionServiceTest"
```

### **Teste Manual:**
```bash
# 1. Gerar chaves
curl -X GET "http://localhost:8084/api/consumer-keys/generate-example"

# 2. Registrar chave pública (copiar do resultado anterior)
curl -X POST "http://localhost:8084/api/consumer-keys" \
     -H "Content-Type: application/json" \
     -d '{"consumerName":"Test","consumerIdentifier":"test-app","publicKey":"..."}' 

# 3. Buscar credenciais criptografadas
curl -X GET "http://localhost:8084/api/credentials/encrypted/test-app"
```

## 📈 Métricas e Monitoramento

### **Métricas Recomendadas:**
- Número de chaves públicas ativas
- Frequência de acesso a credenciais criptografadas
- Tempo de resposta dos endpoints RSA
- Tentativas de acesso com chaves inválidas

### **Logs de Auditoria:**
- Registro de novas chaves públicas
- Acessos a credenciais criptografadas
- Falhas de descriptografia
- Chaves expiradas

## 🎯 Conclusão

A implementação oferece uma solução segura e escalável para distribuir senhas criptografadas para consumidores autorizados da API. A combinação de BCrypt (armazenamento) + RSA (transmissão) garante máxima segurança em todas as camadas.

### **Benefícios Principais:**
✅ **Segurança multicamada**
✅ **Controle granular de acesso**
✅ **Facilidade de integração**
✅ **Compatibilidade multiplataforma**
✅ **Auditoria completa**

### **Próximos Passos:**
1. Implementar rotação automática de chaves
2. Adicionar suporte a múltiplos algoritmos
3. Criar dashboard de gerenciamento
4. Implementar métricas avançadas
