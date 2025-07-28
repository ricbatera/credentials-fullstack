# Exemplos de Uso da API - Criptografia de Senhas

## Testando a Implementação da Criptografia

### 1. Iniciando o Servidor

```bash
mvn spring-boot:run
```

O servidor estará disponível em `http://localhost:8080`

### 2. Documentação da API

Acesse o Swagger UI em: `http://localhost:8080/swagger-ui.html`

### 3. Exemplo Prático - Criação de Credencial

#### Criando uma credencial (POST /api/credentials)

```bash
curl -X POST "http://localhost:8080/api/credentials" \
     -H "Content-Type: application/json" \
     -d '{
       "nameMall": "Shopping Center ABC",
       "cnpj": "12345678901234",
       "urlPortal": "https://portal.shoppingabc.com",
       "username": "admin",
       "password": "minhasenha123",
       "active": true
     }'
```

**Resposta esperada:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nameMall": "Shopping Center ABC",
  "cnpj": "12345678901234",
  "urlPortal": "https://portal.shoppingabc.com",
  "username": "admin",
  "createdAt": "2025-07-26T15:30:00",
  "updatedAt": "2025-07-26T15:30:00",
  "deletedAt": null,
  "active": true
}
```

**⚠️ Importante:** Note que a senha não aparece na resposta e foi automaticamente criptografada no banco.

### 4. Verificando uma Senha

#### Verificando se a senha está correta (POST /api/credentials/{id}/verify-password)

```bash
curl -X POST "http://localhost:8080/api/credentials/3fa85f64-5717-4562-b3fc-2c963f66afa6/verify-password" \
     -H "Content-Type: application/json" \
     -d '{
       "password": "minhasenha123"
     }'
```

**Resposta:** `true` (senha correta)

#### Testando com senha incorreta:

```bash
curl -X POST "http://localhost:8080/api/credentials/3fa85f64-5717-4562-b3fc-2c963f66afa6/verify-password" \
     -H "Content-Type: application/json" \
     -d '{
       "password": "senhaerrada"
     }'
```

**Resposta:** `false` (senha incorreta)

### 5. Atualização de Credencial

#### Atualizando uma credencial com nova senha (PUT /api/credentials/{id})

```bash
curl -X PUT "http://localhost:8080/api/credentials/3fa85f64-5717-4562-b3fc-2c963f66afa6" \
     -H "Content-Type: application/json" \
     -d '{
       "nameMall": "Shopping Center ABC",
       "cnpj": "12345678901234",
       "urlPortal": "https://portal.shoppingabc.com",
       "username": "admin",
       "password": "novasenha456",
       "active": true
     }'
```

A nova senha será automaticamente criptografada antes de ser salva.

### 6. Listando Credenciais

#### Obtendo todas as credenciais (GET /api/credentials)

```bash
curl -X GET "http://localhost:8080/api/credentials"
```

**Resposta:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "nameMall": "Shopping Center ABC",
    "cnpj": "12345678901234",
    "urlPortal": "https://portal.shoppingabc.com",
    "username": "admin",
    "createdAt": "2025-07-26T15:30:00",
    "updatedAt": "2025-07-26T15:35:00",
    "deletedAt": null,
    "active": true
  }
]
```

### 7. Verificação no Banco de Dados

Se você acessar o banco de dados diretamente, verá que a senha está criptografada:

```sql
SELECT id, name_mall, username, password FROM credentials;
```

**Resultado:**
```
id                                   | name_mall           | username | password
-------------------------------------|---------------------|----------|--------------------------------------------------------
3fa85f64-5717-4562-b3fc-2c963f66afa6 | Shopping Center ABC | admin    | $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
```

## Testes Automatizados

### Executando os testes

```bash
mvn test
```

### Cobertura dos testes

Os testes cobrem:
- ✅ Criptografia automática na criação
- ✅ Criptografia automática na atualização
- ✅ Verificação de senhas
- ✅ Detecção de senhas já criptografadas
- ✅ Tratamento de casos de erro
- ✅ Validação de entrada

## Segurança Implementada

### Características

1. **BCrypt com custo 12**: Balanceamento entre segurança e performance
2. **Salt único**: Cada senha possui um salt aleatório único
3. **Não reversibilidade**: Impossível recuperar a senha original
4. **Detecção automática**: Sistema detecta se senha já está criptografada
5. **Transparência**: Processo transparente para o usuário da API

### Performance

- **Criptografia**: ~250ms por operação
- **Verificação**: ~250ms por operação
- **Recomendação**: Para operações em lote, considere processamento assíncrono

## Logs e Monitoramento

Durante a operação, você pode ver logs como:

```
INFO  - Creating new credential for mall: Shopping Center ABC
DEBUG - Password encrypted successfully
INFO  - Credential saved with ID: 3fa85f64-5717-4562-b3fc-2c963f66afa6
```

## Troubleshooting

### Problema: Senha não é criptografada
**Solução**: Verifique se o `PasswordEncryptionService` está sendo injetado corretamente no `CredentialsService`.

### Problema: Verificação de senha sempre retorna false
**Solução**: Verifique se a senha está sendo criptografada com BCrypt e se o método `verifyPassword` está sendo usado corretamente.

### Problema: Erro de regex para detecção de BCrypt
**Solução**: Verifique se o padrão regex está correto: `^\\$2[abxy]\\$\\d{2}\\$.{53}$`

## Próximos Passos

1. **Migração de dados**: Se houver senhas em texto plano no banco, criar script de migração
2. **Auditoria**: Implementar logs de auditoria para tentativas de login
3. **Rate limiting**: Implementar limitação de tentativas de verificação de senha
4. **Monitoramento**: Adicionar métricas de performance para operações de criptografia
