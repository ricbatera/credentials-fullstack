#!/bin/bash

# Script de configuração inicial para Jenkins
# Execute este script no seu servidor Linux para preparar o ambiente

echo "🚀 Configurando ambiente para Jenkins - Credentials Full Stack"
echo "============================================================="

# Verificar se o usuário é root ou tem sudo
if [ "$EUID" -ne 0 ] && ! sudo -n true 2>/dev/null; then
    echo "❌ Este script precisa ser executado como root ou o usuário precisa ter permissões sudo"
    exit 1
fi

# Função para executar comandos com sudo se necessário
run_cmd() {
    if [ "$EUID" -eq 0 ]; then
        "$@"
    else
        sudo "$@"
    fi
}

# 1. Instalar Docker (se não estiver instalado)
echo "🐳 Verificando Docker..."
if ! command -v docker &> /dev/null; then
    echo "Instalando Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    run_cmd sh get-docker.sh
    run_cmd usermod -aG docker jenkins
    run_cmd usermod -aG docker $USER
    echo "✅ Docker instalado com sucesso!"
else
    echo "✅ Docker já está instalado"
fi

# 2. Instalar Docker Compose (se não estiver instalado)
echo "🔧 Verificando Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    echo "Instalando Docker Compose..."
    run_cmd curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    run_cmd chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose instalado com sucesso!"
else
    echo "✅ Docker Compose já está instalado"
fi

# 3. Configurar usuário jenkins no grupo docker
echo "👤 Configurando permissões do usuário jenkins..."
run_cmd usermod -aG docker jenkins

# 4. Criar diretórios necessários
echo "📁 Criando diretórios necessários..."
run_cmd mkdir -p /home/jenkins/credentials/{mysql-data,mysql-init,backend-logs,nginx-conf,nginx-logs}
run_cmd chown -R jenkins:jenkins /home/jenkins/credentials
run_cmd chmod -R 755 /home/jenkins/credentials
run_cmd chmod -R 777 /home/jenkins/credentials/mysql-data

# 5. Configurar firewall (se UFW estiver ativo)
echo "🔥 Configurando firewall..."
if command -v ufw &> /dev/null && ufw status | grep -q "Status: active"; then
    echo "Configurando regras do UFW..."
    run_cmd ufw allow 11003/tcp comment "Credentials Backend"
    run_cmd ufw allow 11080/tcp comment "Credentials Frontend"
    echo "✅ Firewall configurado"
else
    echo "ℹ️  UFW não está ativo ou não está instalado"
fi

# 6. Verificar se Jenkins está rodando
echo "🔍 Verificando Jenkins..."
if systemctl is-active --quiet jenkins; then
    echo "✅ Jenkins está rodando"
    echo "Reiniciando Jenkins para aplicar mudanças de grupo..."
    run_cmd systemctl restart jenkins
else
    echo "❌ Jenkins não está rodando ou não está instalado"
    echo "Para instalar Jenkins, visite: https://www.jenkins.io/doc/book/installing/linux/"
fi

# 7. Teste final
echo "🧪 Testando configuração..."
docker --version
docker-compose --version

echo ""
echo "✅ Configuração concluída!"
echo ""
echo "📋 Próximos passos:"
echo "1. Acesse o Jenkins: http://YOUR_SERVER_IP:8080"
echo "2. Configure as credenciais no Jenkins:"
echo "   - mysql-root-password"
echo "   - mysql-user-password"
echo "3. Crie um novo Pipeline Job"
echo "4. Configure o SCM para: https://github.com/ricbatera/credentials-fullstack.git"
echo "5. Execute o pipeline!"
echo ""
echo "🌐 Após o deploy, sua aplicação estará disponível em:"
echo "   Frontend: http://YOUR_SERVER_IP:11080"
echo "   Backend:  http://YOUR_SERVER_IP:11003"
echo "   Swagger:  http://YOUR_SERVER_IP:11003/swagger-ui.html"
