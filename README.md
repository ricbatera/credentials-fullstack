# Credentials Full Stack - Docker Setup

Esta é uma aplicação completa de gerenciamento de credenciais com:
- **Frontend**: HTML/CSS/JavaScript servido pelo Nginx na porta 11002
- **Backend**: Spring Boot API na porta 8084
- **Banco de dados**: MySQL na porta 3306

## Pré-requisitos

- Docker e Docker Compose instalados
- Portas 3306, 8084 e 11002 disponíveis

## Configuração Inicial

### 1. Preparar Diretórios

Execute o script para criar a estrutura de diretórios necessária:

**Windows PowerShell:**
```powershell
.\setup-directories.ps1
```

**Linux/macOS:**
```bash
chmod +x setup-directories.sh
./setup-directories.sh
```

### 2. Iniciar a Stack

```bash
docker-compose up -d
```

### 3. Verificar Status dos Serviços

```bash
docker-compose ps
```

## Acessos

- **Frontend**: http://localhost:11002
- **Backend API**: http://localhost:8084
- **Swagger UI**: http://localhost:11002/swagger-ui/
- **MySQL**: localhost:3306

## Estrutura de Volumes

Todos os dados persistentes são armazenados em `~/credentials/`:

```
~/credentials/
├── mysql-data/       # Dados do banco MySQL
├── mysql-init/       # Scripts de inicialização do MySQL
├── backend-logs/     # Logs do Spring Boot
├── nginx-conf/       # Configurações do Nginx
└── nginx-logs/       # Logs do Nginx
```

## Comandos Úteis

### Parar todos os serviços
```bash
docker-compose down
```

### Parar e remover volumes
```bash
docker-compose down -v
```

### Ver logs em tempo real
```bash
# Todos os serviços
docker-compose logs -f

# Apenas um serviço
docker-compose logs -f credentials-backend
docker-compose logs -f credentials-frontend
docker-compose logs -f mysql-db
```

### Reconstruir e reiniciar
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## Configurações

### Banco de Dados
- **Host**: mysql-db (interno) / localhost (externo)
- **Porta**: 3306
- **Database**: consultdg-credentials
- **Usuário Root**: root / 178209
- **Usuário App**: credentials_user / credentials_pass

### Backend
- **Porta**: 8084
- **Health Check**: http://localhost:8084/actuator/health
- **Swagger**: http://localhost:11002/swagger-ui/

### Frontend
- **Porta**: 11002
- **Nginx**: Configurado com proxy reverso para o backend
- **API Calls**: Direcionadas automaticamente para o backend via `/api/`

## Troubleshooting

### Verificar se os containers estão rodando
```bash
docker ps
```

### Verificar logs de erro
```bash
docker-compose logs credentials-backend
docker-compose logs mysql-db
```

### Resetar banco de dados
```bash
docker-compose down -v
# Remove os dados em ~/credentials/mysql-data se necessário
docker-compose up -d
```

### Verificar conectividade de rede
```bash
docker-compose exec credentials-backend ping mysql-db
```
