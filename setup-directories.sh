#!/bin/bash

# Script para preparar os diretórios necessários para o docker-compose

echo "Criando estrutura de diretórios em ~/credentials..."

# Criar diretório base
mkdir -p ~/credentials

# Criar subdiretórios para volumes
mkdir -p ~/credentials/mysql-data
mkdir -p ~/credentials/mysql-init
mkdir -p ~/credentials/backend-logs
mkdir -p ~/credentials/nginx-conf
mkdir -p ~/credentials/nginx-logs

# Copiar configuração do nginx
echo "Copiando configuração do Nginx..."
cp nginx.conf ~/credentials/nginx-conf/default.conf

# Definir permissões adequadas
chmod -R 755 ~/credentials

echo "Estrutura de diretórios criada com sucesso!"
echo ""
echo "Diretórios criados:"
echo "  ~/credentials/mysql-data     - Dados do MySQL"
echo "  ~/credentials/mysql-init     - Scripts de inicialização do MySQL"
echo "  ~/credentials/backend-logs   - Logs do backend Spring Boot"
echo "  ~/credentials/nginx-conf     - Configurações do Nginx"
echo "  ~/credentials/nginx-logs     - Logs do Nginx"
echo ""
echo "Para iniciar a stack, execute:"
echo "  docker-compose up -d"
