# Refatoração: Inclusão da Propriedade `passwordOfInvoice`

## Resumo
Foi adicionada a nova propriedade `passwordOfInvoice` (senha da nota fiscal) em todos os DTOs e serviços do sistema de credenciais. Esta refatoração mantém a compatibilidade total com o sistema existente e inclui criptografia RSA para a nova senha quando necessário.

## Alterações Realizadas

### 1. DTOs Atualizados

#### `CredentialsRequestDTO`
- ✅ Adicionada propriedade `passwordOfInvoice`
- ✅ Atualizado construtor para incluir novo campo
- ✅ Adicionados getters e setters
- ✅ Documentação Swagger atualizada

#### `CredentialsResponseDTO`
- ✅ Adicionada propriedade `passwordOfInvoice`
- ✅ Atualizado construtor para incluir novo campo
- ✅ Adicionados getters e setters
- ✅ Documentação Swagger atualizada

#### `CredentialsWithEncryptedPasswordDTO`
- ✅ Adicionada propriedade `encryptedPasswordOfInvoice`
- ✅ Atualizado construtor para incluir novo campo
- ✅ Adicionados getters e setters
- ✅ Documentação Swagger atualizada

#### `BasicCredentialsResponseDTO`
- ✅ Adicionada propriedade `passwordOfInvoice`
- ✅ Atualizado construtor para incluir novo campo
- ✅ Adicionados getters e setters
- ✅ Documentação Swagger atualizada

### 2. Serviços Atualizados

#### `CredentialsService`
- ✅ Método `toEntity()`: agora inclui `passwordOfInvoice`
- ✅ Método `updateEntityFromDTO()`: agora atualiza `passwordOfInvoice`
- ✅ Método `toResponseDTO()`: agora inclui `passwordOfInvoice` na resposta
- ✅ Método `findByIdWithEncryptedPassword()`: criptografa `passwordOfInvoice` com RSA
- ✅ Método `findAllWithEncryptedPassword()`: criptografa `passwordOfInvoice` com RSA
- ✅ Método `toEncryptedPasswordDTO()`: atualizado para incluir senha de NF criptografada

### 3. Controllers Atualizados

#### `ConsumerPublicKeyController`
- ✅ Endpoint `/basic-credentials/{consumerIdentifier}` agora retorna `passwordOfInvoice` criptografada

### 4. Testes Atualizados

#### `CredentialsServiceTest`
- ✅ Dados de teste atualizados para incluir `passwordOfInvoice`
- ✅ Todos os testes continuam passando

## Funcionalidades da Nova Propriedade

### 1. Armazenamento
- A senha da nota fiscal é armazenada em texto plano no campo `password_of_invoice` da tabela `credentials`
- Campo é opcional (nullable)

### 2. Criptografia RSA
- Quando solicitado por consumidores autorizados, a `passwordOfInvoice` é criptografada com a chave pública RSA do consumidor
- Retornada no campo `encryptedPasswordOfInvoice` do DTO `CredentialsWithEncryptedPasswordDTO`

### 3. API Endpoints
Todos os endpoints existentes agora suportam a nova propriedade:

- `GET /api/credentials` - Lista incluindo `passwordOfInvoice`
- `GET /api/credentials/{id}` - Retorna incluindo `passwordOfInvoice`
- `POST /api/credentials` - Aceita `passwordOfInvoice` na criação
- `PUT /api/credentials/{id}` - Aceita `passwordOfInvoice` na atualização
- `GET /api/credentials/encrypted/{consumerIdentifier}` - Retorna `encryptedPasswordOfInvoice`
- `GET /api/credentials/{id}/encrypted/{consumerIdentifier}` - Retorna `encryptedPasswordOfInvoice`

### 4. Endpoints de Robôs
- `GET /api/consumer-public-keys/basic-credentials/{consumerIdentifier}` - Agora inclui `passwordOfInvoice` criptografada

## Exemplo de Uso

### Criação de Credencial com Senha de NF
```json
POST /api/credentials
{
  "nameMall": "Shopping ABC",
  "cnpj": "12345678000195",
  "urlPortal": "https://portal.shopping.com.br",
  "username": "admin",
  "password": "senha123",
  "passwordOfInvoice": "senhaNotaFiscal456",
  "active": true
}
```

### Resposta com Senha de NF Criptografada
```json
GET /api/credentials/1/encrypted/robot-consumidor
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nameMall": "Shopping ABC",
  "cnpj": "12345678000195",
  "urlPortal": "https://portal.shopping.com.br",
  "username": "admin",
  "encryptedPassword": "Base64EncryptedPassword==",
  "encryptedPasswordOfInvoice": "Base64EncryptedInvoicePassword==",
  "consumerIdentifier": "robot-consumidor",
  "encryptionAlgorithm": "RSA",
  "createdAt": "2025-07-27T10:30:00",
  "updatedAt": "2025-07-27T15:45:00",
  "deletedAt": null,
  "active": true
}
```

## Compatibilidade
- ✅ 100% compatível com código existente
- ✅ Campo opcional, não quebra integrações existentes
- ✅ Todos os testes passando
- ✅ Endpoints existentes continuam funcionando normalmente

## Status
- ✅ Refatoração Completa
- ✅ Testes Passando
- ✅ Compilação Bem-sucedida
- ✅ Pronto para Deploy
