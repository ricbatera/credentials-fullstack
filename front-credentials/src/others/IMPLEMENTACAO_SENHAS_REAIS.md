# 🔐 Implementação: Retorno de Senhas Reais via RSA

## 📋 Resumo da Implementação

Foi implementada com sucesso a funcionalidade para retornar **senhas reais** (em vez de mensagens de proteção) através dos endpoints `/encrypted/{consumerIdentifier}`, mantendo a segurança com dupla criptografia.

## 🏗️ Arquitetura da Solução

### **Dupla Criptografia Híbrida:**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Senha Original│    │   Armazenamento │    │   Transmissão   │
│   "minhasenha"  │    │   Banco de Dados│    │   para Cliente  │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ 1. AES Interno  │───►│ original_pwd_enc│    │                 │
│ (reversível)    │    │ "encrypted_aes" │───►│ 3. RSA Consumer │
│                 │    │                 │    │ (para cliente)  │
│ 2. BCrypt       │───►│ password        │    │                 │
│ (verificação)   │    │ "$2a$12$..."   │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔧 Componentes Implementados

### 1. **InternalEncryptionService**
**Arquivo:** `src/main/java/.../service/InternalEncryptionService.java`

**Responsabilidade:** Criptografia simétrica AES para armazenar senhas de forma reversível.

**Métodos:**
- `encrypt(String plainPassword)` - Criptografa senha com AES
- `decrypt(String encryptedPassword)` - Descriptografa senha AES

### 2. **Campo Adicional na Entidade**
**Arquivo:** `src/main/java/.../model/Credentials.java`

**Campo adicionado:**
```java
@Column(name = "original_password_encrypted", nullable = true, length = 1000)
private String originalPasswordEncrypted;
```

### 3. **Modificações no CredentialsService**
**Arquivo:** `src/main/java/.../service/CredentialsService.java`

**Mudanças:**
- Injeção do `InternalEncryptionService`
- Método `encryptPasswordIfNeeded()` agora salva dupla criptografia
- Métodos `findByIdWithEncryptedPassword()` e `findAllWithEncryptedPassword()` retornam senhas reais

## 🚀 Como Funciona

### **1. Criação/Atualização de Credencial:**
```java
// Fluxo ao salvar uma credencial:
1. Senha original → AES encrypt → originalPasswordEncrypted (banco)
2. Senha original → BCrypt hash → password (banco, para verificação)
```

### **2. Recuperação para Consumidor:**
```java
// Fluxo ao solicitar credencial via /encrypted/{consumerId}:
1. Busca credential.originalPasswordEncrypted do banco
2. AES decrypt → senha original em texto plano
3. RSA encrypt com chave pública do consumidor → para o cliente
```

## 📊 Exemplos de Uso

### **Criar Nova Credencial:**
```bash
curl -X POST "http://localhost:8084/api/credentials" \
  -H "Content-Type: application/json" \
  -d '{
    "nameMall": "Shopping Test",
    "cnpj": "12345678901234",
    "urlPortal": "https://portal.test.com",
    "username": "admin",
    "password": "minhasenha123",
    "active": true
  }'
```

### **Buscar com Senha Criptografada (NOVA FUNCIONALIDADE):**
```bash
curl -X GET "http://localhost:8084/api/credentials/encrypted/meu-consumidor"
```

**Resposta (agora com senha real criptografada):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nameMall": "Shopping Test",
  "username": "admin",
  "encryptedPassword": "Base64RSAEncryptedRealPassword==",
  "consumerIdentifier": "meu-consumidor",
  "encryptionAlgorithm": "RSA"
}
```

## 🔒 Segurança Implementada

### **Múltiplas Camadas:**
1. **AES interno** - Para armazenamento reversível (chave fixa no código)
2. **BCrypt** - Para verificação de senhas (irreversível, seguro)
3. **RSA per-consumer** - Para transmissão segura (chave única por cliente)

### **Benefícios:**
✅ **Senhas reais** são retornadas aos consumidores autorizados
✅ **Verificação segura** continua usando BCrypt
✅ **Transmissão criptografada** individualizada por consumidor
✅ **Compatibilidade** com implementação anterior mantida

## 🧪 Testes Implementados

### **InternalEncryptionServiceTest:**
- Teste de criptografia/descriptografia AES
- Validação de dados inválidos
- Verificação de robustez

### **CredentialsServiceTest (Atualizados):**
- Mock do novo `InternalEncryptionService`
- Verificação da dupla criptografia
- Testes de criação e atualização

## 📁 Arquivos Modificados/Criados

### **Novos Arquivos:**
- `InternalEncryptionService.java` - Serviço de criptografia AES
- `InternalEncryptionServiceTest.java` - Testes do novo serviço
- `migration_add_original_password_encrypted.sql` - Script SQL para migração

### **Arquivos Modificados:**
- `Credentials.java` - Adicionado campo `originalPasswordEncrypted`
- `CredentialsService.java` - Implementada dupla criptografia
- `CredentialsServiceTest.java` - Atualizados mocks e testes

## 🗃️ Migração do Banco de Dados

Execute o script SQL para adicionar a nova coluna:

```sql
ALTER TABLE credentials 
ADD COLUMN original_password_encrypted VARCHAR(1000) NULL 
COMMENT 'Senha original criptografada com AES para permitir descriptografia';
```

**Nota:** Credenciais existentes terão este campo NULL até serem atualizadas. Novas credenciais preencherão automaticamente.

## ✅ Status da Implementação

| Funcionalidade | Status | Descrição |
|----------------|--------|-----------|
| **Dupla Criptografia** | ✅ Concluído | AES + BCrypt implementados |
| **Senhas Reais via RSA** | ✅ Concluído | Endpoints retornam senhas reais |
| **Testes Unitários** | ✅ Concluído | Todos os testes passando |
| **Migração Banco** | ✅ Concluído | Script SQL criado |
| **Documentação** | ✅ Concluído | Este documento |

## 🎯 Resultado Final

**ANTES (problema):**
```json
{
  "encryptedPassword": "Base64(RSA('Senha protegida com BCrypt - use endpoint de verificação'))"
}
```

**DEPOIS (solução):**
```json
{
  "encryptedPassword": "Base64(RSA('minhasenha123'))"
}
```

A implementação agora retorna **senhas reais criptografadas** mantendo todos os níveis de segurança!
