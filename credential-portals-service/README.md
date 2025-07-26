# Credential Portals Service

Serviço Spring Boot para gerenciamento de credenciais com Docker Compose.

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
