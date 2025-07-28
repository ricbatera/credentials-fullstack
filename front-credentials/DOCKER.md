# Docker Setup para Front-Credentials

Este projeto Angular está configurado para rodar em um container Docker com nginx.

## Arquivos Docker

- `Dockerfile`: Configuração multi-stage para build e deploy
- `nginx.conf`: Configuração customizada do nginx para SPA
- `docker-compose.yml`: Orquestração de containers
- `.dockerignore`: Arquivos excluídos do build

## Como usar

### Opção 1: Docker Build e Run

```bash
# Build da imagem
docker build -t front-credentials .

# Executar o container
docker run -d -p 8080:80 --name front-credentials-app front-credentials
```

### Opção 2: Docker Compose (Recomendado)

```bash
# Build e executar
docker-compose up -d

# Parar e remover
docker-compose down
```

### Opção 3: Apenas Build (sem executar)

```bash
# Apenas fazer o build
docker-compose build
```

## Acessar a aplicação

Após executar qualquer uma das opções acima, a aplicação estará disponível em:
- http://localhost:8080

## Comandos úteis

```bash
# Ver logs do container
docker logs front-credentials-app

# Executar comandos dentro do container
docker exec -it front-credentials-app sh

# Parar o container
docker stop front-credentials-app

# Remover o container
docker rm front-credentials-app

# Remover a imagem
docker rmi front-credentials
```

## Healthcheck

O nginx está configurado com um endpoint de healthcheck em `/health` que retorna "healthy" se o serviço estiver funcionando.

## Configurações do nginx

- Suporte completo para SPA (Single Page Application)
- Cache de assets estáticos por 1 ano
- Compressão gzip habilitada
- Headers de segurança básicos
- Endpoint de healthcheck

## Variáveis de ambiente

As configurações da aplicação estão no arquivo `src/assets/appConfig.json`. Se precisar de diferentes configurações para produção, você pode:

1. Criar diferentes arquivos de configuração
2. Usar variáveis de ambiente no container
3. Montar volumes com configurações específicas

## Produção

Para produção, considere:

1. Usar um registry de imagens Docker
2. Configurar HTTPS no nginx ou usar um proxy reverso
3. Ajustar as configurações de cache conforme necessário
4. Implementar monitoramento e logs centralizados
