# Instruções de Teste - Criptografia RSA

## 1. Gerar Chaves RSA

1. Abra o arquivo `generate-keys.html` no navegador
2. Clique em "Gerar Chaves RSA"
3. Copie a chave pública e privada geradas

## 2. Registrar Chave Pública na API

Use este curl para registrar a chave pública:

```bash
curl -X POST http://localhost:8084/api/consumer-keys \
  -H "Content-Type: application/json" \
  -d '{
    "consumerName": "Frontend Credentials App",
    "consumerIdentifier": "frontend-app-v1",
    "publicKey": "SUA_CHAVE_PUBLICA_BASE64_AQUI",
    "description": "Chave pública para aplicação frontend de teste"
  }'
```

## 3. Verificar Chave Registrada

```bash
curl http://localhost:8084/api/consumer-keys/consumer/frontend-app-v1/valid
```

## 4. Configurar Frontend

Edite o arquivo `config.js` com as chaves geradas:

```javascript
window.AppConfig = {
    API_BASE_URL: 'http://localhost:8084/api/credentials',
    CONSUMER_IDENTIFIER: 'frontend-app-v1',
    PRIVATE_KEY: `-----BEGIN PRIVATE KEY-----
SUA_CHAVE_PRIVADA_AQUI
-----END PRIVATE KEY-----`,
    PUBLIC_KEY: 'SUA_CHAVE_PUBLICA_BASE64_AQUI'
};
```

## 5. Testar Funcionalidades

### Com Criptografia (Chaves Configuradas)
1. Abra o `index.html` no navegador
2. Verifique no console se as chaves foram inicializadas
3. Adicione uma credencial
4. Verifique se as senhas são exibidas corretamente

### Endpoints que devem ser chamados:
- `GET /api/credentials/encrypted/frontend-app-v1` (listar)
- `GET /api/credentials/{id}/encrypted/frontend-app-v1` (detalhes)

### Sem Criptografia (Modo Compatibilidade)
1. Remova as chaves do `config.js` (deixe vazios)
2. Recarregue a página
3. Verifique se o sistema funciona normalmente
4. Deve usar os endpoints tradicionais da API

## 6. Debugging

### Verificar no Console do Browser:
- Mensagens de inicialização RSA
- Logs de criptografia/descriptografia
- Erros de conexão com API

### Comandos úteis:
```bash
# Verificar se chave está válida na API
curl http://localhost:8084/api/consumer-keys/consumer/frontend-app-v1

# Listar todas as chaves públicas
curl http://localhost:8084/api/consumer-keys

# Buscar credencial com senha criptografada
curl http://localhost:8084/api/credentials/{ID}/encrypted/frontend-app-v1
```

## 7. Teste com Docker

```bash
# Build da imagem
docker build -t credentials-frontend .

# Executar com as chaves (substitua pelas suas chaves)
docker run -p 8080:80 \
  -e API_BASE_URL=http://localhost:8084/api/credentials \
  -e CONSUMER_IDENTIFIER=frontend-app-v1 \
  -e PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nSUA_CHAVE_PRIVADA\n-----END PRIVATE KEY-----" \
  -e PUBLIC_KEY="SUA_CHAVE_PUBLICA_BASE64" \
  credentials-frontend

# Acesse http://localhost:8080
```

## 8. Cenários de Teste

### Cenário 1: Primeira Configuração
- [ ] Gerar chaves RSA
- [ ] Registrar chave pública na API
- [ ] Configurar frontend
- [ ] Verificar funcionamento com criptografia

### Cenário 2: Fallback para Compatibilidade
- [ ] Remover chaves do config.js
- [ ] Verificar se sistema continua funcionando
- [ ] Verificar se usa endpoints tradicionais

### Cenário 3: Chave Inválida
- [ ] Configurar chave pública incorreta
- [ ] Verificar se sistema detecta erro
- [ ] Verificar se faz fallback automático

### Cenário 4: Migration de Sistema Antigo
- [ ] Começar sem chaves configuradas
- [ ] Adicionar chaves depois
- [ ] Verificar se sistema migra automaticamente

## 9. Indicadores de Sucesso

✅ **Criptografia Funcionando:**
- Console mostra "Chave pública registrada com sucesso"
- Senhas são exibidas corretamente após descriptografia
- Endpoints `/encrypted/` são usados nas requisições

✅ **Modo Compatibilidade:**
- Console mostra "Chaves RSA não configuradas. Usando modo compatibilidade"
- Sistema funciona normalmente
- Endpoints tradicionais são usados

❌ **Problemas Comuns:**
- "Erro ao configurar criptografia" - Verificar chaves
- "Consumidor não possui chave pública válida" - Registrar na API
- Senhas não descriptografam - Verificar correspondência das chaves
