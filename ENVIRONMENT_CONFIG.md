# Configuração de Variáveis de Ambiente

## Como configurar a URL da API

### Método 1: Usando arquivo .env (Recomendado)

1. Edite o arquivo `.env` na raiz do projeto
2. Modifique a variável `API_BASE_URL`:
```bash
API_BASE_URL=http://201.77.115.146:11003/api/credentials
```

### Método 2: Definindo variáveis de ambiente diretamente

No PowerShell (Windows):
```powershell
$env:API_BASE_URL = "http://201.77.115.146:11003/api/credentials"
docker-compose up -d
```

No terminal Linux/Mac:
```bash
export API_BASE_URL="http://201.77.115.146:11003/api/credentials"
docker-compose up -d
```

### Método 3: Inline com docker-compose

```bash
API_BASE_URL="http://201.77.115.146:11003/api/credentials" docker-compose up -d
```

## Como funciona

1. O arquivo `config.js.template` contém um placeholder `${API_BASE_URL}`
2. Durante a inicialização do container, o script `entrypoint.sh` executa `envsubst` para substituir o placeholder pela variável de ambiente
3. O resultado é salvo em `config.js` que é carregado pelo `index.html`
4. O JavaScript usa `window.AppConfig.API_BASE_URL` para fazer as requisições

## Desenvolvimento Local

Para desenvolvimento local, você pode usar diretamente:
- `http://localhost:8084/api/credentials` (backend local)
- `http://201.77.115.146:11003/api/credentials` (backend remoto)

## Outras Variáveis Configuráveis

- `MYSQL_ROOT_PASSWORD`: Senha do root do MySQL
- `MYSQL_DATABASE`: Nome do banco de dados
- `MYSQL_USER`: Usuário do MySQL
- `MYSQL_PASSWORD`: Senha do usuário MySQL
- `BACKEND_PORT`: Porta do backend (padrão: 8084)
- `FRONTEND_PORT`: Porta do frontend (padrão: 11002)
