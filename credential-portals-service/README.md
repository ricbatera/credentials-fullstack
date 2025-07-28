# Credential Portals Service 🔐

Serviço Spring Boot para gerenciamento **seguro** de credenciais com **criptografia multicamadas** e Docker Compose.

## ✨ Funcionalidades

### 🔒 **Segurança Multicamadas**
- **BCrypt**: Senhas armazenadas no banco com hash irreversível (cost factor 12)
- **RSA 2048-bit**: Criptografia assimétrica para transmissão segura aos consumidores da API
- **Controle de Acesso**: Apenas consumidores registrados podem acessar senhas criptografadas

### 🛡️ **Criptografia RSA para Consumidores**
- Cada consumidor da API possui um par de chaves RSA (pública/privada)
- Chave pública registrada na API, privada mantida pelo consumidor
- Senhas são criptografadas individualmente para cada consumidor autorizado
- Suporte completo para expiração e rotação de chaves

### 🌐 **API Completa**
- **CRUD de Credenciais**: Operações completas com validação
- **Gerenciamento de Chaves**: Registro, validação e controle de chaves públicas
- **Endpoints Seguros**: Credenciais com senhas criptografadas por consumidor
- **Documentação Swagger**: Interface interativa para testes

## 📚 Documentação Completa

- **[📋 Guia de Implementação RSA](GUIA_CRIPTOGRAFIA_RSA.md)** - Guia completo da solução
- **[👨‍💻 Exemplos de Implementação](EXEMPLOS_IMPLEMENTACAO_RSA.md)** - Código em Java, Node.js, Python, Flutter
- **[🧪 Scripts de Teste](SCRIPTS_TESTE_RSA.md)** - Scripts para validação e testes

## Pré-requisitos

- Docker
- Docker Compose

## Como usar

### 1. Configurar variáveis de ambiente

Copie o arquivo de exemplo e configure suas variáveis:

```bash
cp .env.example .env
```

Edite o arquivo `.env` conforme necessário.

### 2. Executar com Docker Compose

```bash
# Construir e iniciar os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar os serviços
docker-compose down

# Parar e remover volumes (cuidado: remove dados do banco)
docker-compose down -v
```

### 3. Acessar a aplicação

- **API**: http://localhost:8084
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **API Docs**: http://localhost:8084/api-docs

### 4. Banco de dados

O MySQL estará disponível em:
- **Host**: localhost
- **Porta**: 3306 (ou conforme configurado no .env)
- **Database**: consultdg-credentials
- **Username**: root
- **Password**: 178209 (ou conforme configurado no .env)

## 🚀 Teste Rápido da Funcionalidade RSA

### **Passo 1: Gerar Par de Chaves**
```bash
curl -X GET "http://localhost:8084/api/consumer-keys/generate-example"
```

### **Passo 2: Registrar Chave Pública**
```bash
curl -X POST "http://localhost:8084/api/consumer-keys" \
     -H "Content-Type: application/json" \
     -d '{
       "consumerName": "Minha Aplicação",
       "consumerIdentifier": "minha-app-v1",
       "publicKey": "SUA_CHAVE_PUBLICA_AQUI",
       "description": "Teste da funcionalidade RSA"
     }'
```

### **Passo 3: Buscar Credenciais Criptografadas**
```bash
curl -X GET "http://localhost:8084/api/credentials/encrypted/minha-app-v1"
```

### **Passo 4: Descriptografar com Chave Privada**
Use sua chave privada para descriptografar as senhas retornadas!

## 📊 Endpoints da API

### **Credenciais Tradicionais**
- `GET /api/credentials` - Lista todas as credenciais (sem senhas)
- `POST /api/credentials` - Cria nova credencial
- `GET /api/credentials/{id}` - Busca credencial por ID (sem senha)
- `PUT /api/credentials/{id}` - Atualiza credencial
- `DELETE /api/credentials/{id}` - Remove credencial

### **🔐 Credenciais com Criptografia RSA**
- `GET /api/credentials/encrypted/{consumerId}` - Lista todas com senhas criptografadas
- `GET /api/credentials/{id}/encrypted/{consumerId}` - Busca uma com senha criptografada
- `POST /api/credentials/encrypt-password/{consumerId}` - Criptografa senha específica

### **🔑 Gerenciamento de Chaves Públicas**
- `GET /api/consumer-keys` - Lista todas as chaves públicas
- `POST /api/consumer-keys` - Registra nova chave pública
- `GET /api/consumer-keys/consumer/{id}` - Busca chave por consumidor
- `DELETE /api/consumer-keys/{id}` - Remove chave
- `GET /api/consumer-keys/generate-example` - Gera exemplo de par de chaves

## 🔒 Arquitetura de Segurança

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Consumidor    │    │   API Service   │    │   Banco de      │
│   (Frontend)    │    │                 │    │   Dados         │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Chave Privada   │    │ Chave Pública   │    │ Senhas BCrypt   │
│ (Descriptografa)│◄──►│ (Criptografa)   │◄──►│ (Armazenadas)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Fluxo de Segurança:**
1. **Armazenamento**: Senhas → BCrypt → Banco de Dados ✓
2. **Registro**: Consumidor registra chave pública → API ✓
3. **Transmissão**: API criptografa com RSA → Consumidor ✓
4. **Uso**: Consumidor descriptografa com chave privada ✓

## 🧪 Testando a Implementação

Execute os testes automatizados:

```bash
# Testes unitários completos
mvn test

# Testes específicos do RSA
mvn test -Dtest="RSAEncryptionServiceTest"

# Teste completo com script
./test_rsa_complete.sh
```

## 🛠️ Tecnologias Utilizadas

- **Spring Boot 3.5.3** - Framework principal
- **Spring Security** - Criptografia BCrypt
- **JPA/Hibernate** - Persistência de dados  
- **MySQL** - Banco de dados principal
- **H2** - Banco para testes
- **Swagger/OpenAPI** - Documentação da API
- **Docker & Docker Compose** - Containerização
- **RSA 2048-bit** - Criptografia assimétrica
- **Maven** - Gerenciamento de dependências

## 📈 Métricas e Performance

A implementação RSA suporta:
- **Throughput**: > 10 requests/segundo para criptografia
- **Latência**: < 500ms para operações RSA
- **Segurança**: Chaves RSA 2048-bit (padrão industrial)
- **Escalabilidade**: Múltiplos consumidores simultâneos

## 🎯 Casos de Uso

### **Frontend Web/Mobile**
Aplicações que precisam conectar em múltiplos portais de forma segura

### **Serviços de Integração** 
Microserviços que fazem scraping ou automação em portais

### **Ferramentas de Automação**
Scripts e robôs que precisam de credenciais seguras

### **Dashboards Corporativos**
Painéis que agregam dados de múltiplas fontes

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch: `git checkout -b feature/nova-funcionalidade`
3. Commit: `git commit -m 'Adiciona nova funcionalidade'`
4. Push: `git push origin feature/nova-funcionalidade`
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.

## Variáveis de ambiente disponíveis

| Variável | Descrição | Valor padrão |
|----------|-----------|--------------|
| `DB_NAME` | Nome do banco de dados | `consultdg-credentials` |
| `DB_USER` | Usuário do banco | `root` |
| `DB_PASSWORD` | Senha do banco | `178209` |
| `DB_PORT` | Porta do MySQL | `3306` |
| `APP_PORT` | Porta da aplicação | `8084` |
| `DDL_AUTO` | Hibernate DDL mode | `update` |
| `SHOW_SQL` | Mostrar SQL no log | `true` |
| `FORMAT_SQL` | Formatar SQL no log | `true` |

## Desenvolvimento

Para desenvolvimento local sem Docker:

```bash
# Executar apenas o MySQL
docker-compose up mysql -d

# Executar a aplicação localmente
./mvnw spring-boot:run
```
