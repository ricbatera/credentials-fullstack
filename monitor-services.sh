#!/bin/bash

# Script de monitoramento para Credentials Full Stack
# Execute este script para verificar o status dos serviços

PROJECT_NAME="credentials-fullstack"
BACKEND_PORT="11003"
FRONTEND_PORT="11080"

echo "🔍 Monitoramento - Credentials Full Stack"
echo "========================================"
echo "Data/Hora: $(date)"
echo ""

# Verificar se Docker está rodando
echo "🐳 Status do Docker:"
if systemctl is-active --quiet docker; then
    echo "✅ Docker está rodando"
else
    echo "❌ Docker não está rodando"
    exit 1
fi
echo ""

# Status dos containers
echo "📦 Status dos Containers:"
docker-compose -p $PROJECT_NAME ps
echo ""

# Verificar saúde dos serviços
echo "🏥 Health Checks:"

# MySQL
echo "🗄️  MySQL:"
if docker-compose -p $PROJECT_NAME exec -T mysql-db mysqladmin ping -h localhost --silent; then
    echo "✅ MySQL está respondendo"
else
    echo "❌ MySQL não está respondendo"
fi

# Backend
echo "🔧 Backend:"
if curl -f -s http://localhost:$BACKEND_PORT/actuator/health > /dev/null; then
    echo "✅ Backend está respondendo"
    echo "   Health: $(curl -s http://localhost:$BACKEND_PORT/actuator/health | jq -r '.status' 2>/dev/null || echo 'OK')"
else
    echo "❌ Backend não está respondendo"
fi

# Frontend
echo "🌐 Frontend:"
if curl -f -s http://localhost:$FRONTEND_PORT/ > /dev/null; then
    echo "✅ Frontend está respondendo"
else
    echo "❌ Frontend não está respondendo"
fi
echo ""

# Uso de recursos
echo "💾 Uso de Recursos:"
echo "Memória:"
free -h | head -2
echo ""
echo "Disco:"
df -h | grep -E '(Filesystem|/$|/var|/home)'
echo ""

# Logs recentes (últimas 10 linhas)
echo "📋 Logs Recentes (últimas 10 linhas):"
echo ""
echo "=== MySQL ==="
docker-compose -p $PROJECT_NAME logs --tail=10 mysql-db 2>/dev/null || echo "Container não encontrado"
echo ""
echo "=== Backend ==="
docker-compose -p $PROJECT_NAME logs --tail=10 credentials-backend 2>/dev/null || echo "Container não encontrado"
echo ""
echo "=== Frontend ==="
docker-compose -p $PROJECT_NAME logs --tail=10 credentials-frontend 2>/dev/null || echo "Container não encontrado"
echo ""

# Resumo
echo "📊 Resumo:"
containers_running=$(docker-compose -p $PROJECT_NAME ps --services --filter status=running | wc -l)
total_containers=$(docker-compose -p $PROJECT_NAME ps --services | wc -l)

if [ $containers_running -eq $total_containers ] && [ $total_containers -gt 0 ]; then
    echo "✅ Todos os serviços estão rodando ($containers_running/$total_containers)"
    echo ""
    echo "🌐 Acessos:"
    echo "   Frontend: http://$(hostname -I | awk '{print $1}'):$FRONTEND_PORT"
    echo "   Backend:  http://$(hostname -I | awk '{print $1}'):$BACKEND_PORT"
    echo "   Swagger:  http://$(hostname -I | awk '{print $1}'):$BACKEND_PORT/swagger-ui.html"
else
    echo "⚠️  Alguns serviços podem não estar rodando ($containers_running/$total_containers)"
fi

echo ""
echo "🔧 Comandos úteis:"
echo "   Parar todos: docker-compose -p $PROJECT_NAME down"
echo "   Iniciar todos: docker-compose -p $PROJECT_NAME up -d"
echo "   Ver logs: docker-compose -p $PROJECT_NAME logs -f [service]"
echo "   Rebuild: docker-compose -p $PROJECT_NAME up -d --build"
