# Criptografia de Senhas - Credential Portals Service

## Visão Geral

Este documento descreve a implementação da criptografia de senhas no serviço de credenciais dos portais. A solução utiliza o algoritmo BCrypt para garantir a segurança das senhas armazenadas no banco de dados.

## Características da Implementação

### Algoritmo de Criptografia
- **BCrypt**: Utiliza o algoritmo BCrypt com fator de custo 12
- **Salt**: Cada senha possui um salt único e aleatório
- **Segurança**: Resistente a ataques de força bruta e rainbow table

### Funcionalidades

#### 1. Criptografia Automática
- Senhas são automaticamente criptografadas antes de serem salvas no banco
- Processo transparente para o usuário da API
- Verificação se a senha já está criptografada para evitar dupla criptografia

#### 2. Verificação de Senhas
- Endpoint dedicado para verificação de senhas
- Comparação segura entre senha em texto plano e hash armazenado
- Retorno booleano indicando se a senha está correta

#### 3. Detecção de Senhas Criptografadas
- Método para identificar se uma senha já está criptografada
- Baseado no padrão do BCrypt: `$2a$12$...`
- Previne criptografia desnecessária de senhas já protegidas

## Estrutura dos Componentes

### 1. PasswordEncryptionService
**Localização**: `src/main/java/.../service/PasswordEncryptionService.java`

**Responsabilidades**:
- Criptografar senhas em texto plano
- Verificar correspondência entre senha plana e criptografada
- Identificar se uma senha já está criptografada

**Métodos principais**:
```java
public String encryptPassword(String plainPassword)
public boolean verifyPassword(String plainPassword, String encryptedPassword)
public boolean isPasswordEncrypted(String password)
```

### 2. Credentials Entity
**Localização**: `src/main/java/.../model/Credentials.java`

**Melhorias implementadas**:
- Campo transient `passwordChanged` para controlar quando criptografar
- Métodos auxiliares para gerenciar o estado da senha
- Setter personalizado que detecta mudanças na senha

**Métodos adicionados**:
```java
public boolean isPasswordEncrypted()
public void markPasswordAsChanged()
public boolean isPasswordChanged()
public void markPasswordAsProcessed()
```

### 3. CredentialsService
**Localização**: `src/main/java/.../service/CredentialsService.java`

**Integrações**:
- Injeção do `PasswordEncryptionService`
- Criptografia automática nos métodos `create` e `update`
- Método `verifyPassword` para validação de senhas

### 4. CredentialsController
**Localização**: `src/main/java/.../api/controller/CredentialsController.java`

**Novo endpoint**:
```
POST /api/credentials/{id}/verify-password
```

## Configuração de Segurança

### SecurityConfig
**Localização**: `src/main/java/.../config/SecurityConfig.java`

- Desabilita autenticação padrão do Spring Security
- Permite uso apenas do BCrypt para criptografia
- Configuração minimalista focada na proteção de senhas

## Fluxo de Operação

### 1. Criação de Credencial
```
Cliente → RequestDTO → Service.create() → encryptPasswordIfNeeded() → Repository.save()
```

### 2. Atualização de Credencial
```
Cliente → RequestDTO → Service.update() → encryptPasswordIfNeeded() → Repository.save()
```

### 3. Verificação de Senha
```
Cliente → PasswordVerificationRequestDTO → Service.verifyPassword() → PasswordEncryptionService.verifyPassword()
```

## Testes

### Testes Unitários Implementados

#### PasswordEncryptionServiceTest
- Criptografia de senhas válidas
- Tratamento de entradas inválidas (null, vazio, espaços)
- Verificação de senhas corretas e incorretas
- Detecção de senhas já criptografadas
- Geração de hashes únicos para mesma senha

#### CredentialsServiceTest
- Criptografia automática na criação
- Criptografia automática na atualização
- Verificação de senhas via serviço
- Tratamento de casos de erro

## Exemplos de Uso

### 1. Criação de Credencial com Senha
```json
POST /api/credentials
{
  "nameMall": "Shopping ABC",
  "cnpj": "12345678901234",
  "urlPortal": "https://portal.shoppingabc.com",
  "username": "admin",
  "password": "minhasenha123",
  "active": true
}
```
**Resultado**: Senha "minhasenha123" é automaticamente criptografada antes de ser salva.

### 2. Verificação de Senha
```json
POST /api/credentials/{id}/verify-password
{
  "password": "minhasenha123"
}
```
**Resposta**: `true` se a senha estiver correta, `false` caso contrário.

## Segurança

### Características de Segurança
- **Fator de Custo 12**: Balanceamento entre segurança e performance
- **Salt Único**: Cada senha possui um salt aleatório único
- **Resistência a Ataques**: Proteção contra força bruta e rainbow tables
- **Não Reversibilidade**: Impossível recuperar senha original do hash

### Boas Práticas Implementadas
- Validação de entrada (senha não pode ser nula ou vazia)
- Detecção de senhas já criptografadas
- Testes abrangentes para verificar segurança
- Separação de responsabilidades entre componentes

## Dependências Adicionadas

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## Considerações de Performance

- **BCrypt Custo 12**: Aproximadamente 250ms por operação em hardware médio
- **Operações Assíncronas**: Recomendado para verificações em lote
- **Cache de Verificações**: Pode ser implementado se necessário

## Migração de Dados Existentes

Caso existam senhas em texto plano no banco:

1. **Script de Migração**: Criar script para criptografar senhas existentes
2. **Detecção Automática**: Sistema detecta senhas não criptografadas
3. **Criptografia Progressiva**: Senhas são criptografadas conforme são atualizadas

## Monitoramento e Logs

- Logs de operações de criptografia (sem exposição de senhas)
- Métricas de performance para operações BCrypt
- Alertas para tentativas de acesso com senhas incorretas

## Conclusão

A implementação garante que todas as senhas sejam armazenadas de forma segura no banco de dados, utilizando as melhores práticas de criptografia. O sistema é transparente para os usuários da API e mantém compatibilidade com o código existente.
