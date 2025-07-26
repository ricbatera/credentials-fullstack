#!/bin/bash

# Script de backup para MySQL do Credentials Full Stack
# Execute este script para fazer backup do banco de dados

PROJECT_NAME="credentials-fullstack"
BACKUP_DIR="$HOME/credentials/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="credentials_db"

echo "💾 Backup do Banco de Dados - Credentials Full Stack"
echo "=================================================="
echo "Data/Hora: $(date)"
echo ""

# Criar diretório de backup se não existir
mkdir -p "$BACKUP_DIR"

# Verificar se o container MySQL está rodando
if ! docker-compose -p $PROJECT_NAME ps mysql-db | grep -q "Up"; then
    echo "❌ Container MySQL não está rodando!"
    echo "Execute: docker-compose -p $PROJECT_NAME up -d mysql-db"
    exit 1
fi

echo "🗄️  Fazendo backup do banco de dados..."

# Fazer backup
BACKUP_FILE="$BACKUP_DIR/credentials_db_backup_$DATE.sql"

if docker-compose -p $PROJECT_NAME exec -T mysql-db mysqldump \
    -u root \
    -p"$(docker-compose -p $PROJECT_NAME exec -T mysql-db printenv MYSQL_ROOT_PASSWORD 2>/dev/null | tr -d '\r')" \
    --single-transaction \
    --routines \
    --triggers \
    "$DB_NAME" > "$BACKUP_FILE"; then
    
    echo "✅ Backup criado com sucesso!"
    echo "📁 Arquivo: $BACKUP_FILE"
    echo "📏 Tamanho: $(du -h "$BACKUP_FILE" | cut -f1)"
else
    echo "❌ Erro ao criar backup!"
    exit 1
fi

# Comprimir backup
echo "🗜️  Comprimindo backup..."
if gzip "$BACKUP_FILE"; then
    BACKUP_FILE="${BACKUP_FILE}.gz"
    echo "✅ Backup comprimido: $BACKUP_FILE"
    echo "📏 Tamanho comprimido: $(du -h "$BACKUP_FILE" | cut -f1)"
fi

# Listar backups existentes
echo ""
echo "📋 Backups existentes:"
ls -lah "$BACKUP_DIR"/credentials_db_backup_*.sql.gz 2>/dev/null | tail -10 || echo "Nenhum backup encontrado"

# Limpeza automática (manter apenas os últimos 7 backups)
echo ""
echo "🧹 Limpando backups antigos (mantendo os últimos 7)..."
cd "$BACKUP_DIR" && ls -t credentials_db_backup_*.sql.gz 2>/dev/null | tail -n +8 | xargs -r rm -f
echo "✅ Limpeza concluída"

echo ""
echo "📝 Instruções para restauração:"
echo "1. Parar a aplicação: docker-compose -p $PROJECT_NAME down"
echo "2. Remover dados antigos: rm -rf ~/credentials/mysql-data/*"
echo "3. Iniciar apenas MySQL: docker-compose -p $PROJECT_NAME up -d mysql-db"
echo "4. Aguardar MySQL inicializar (~30 segundos)"
echo "5. Restaurar backup:"
echo "   zcat $BACKUP_FILE | docker-compose -p $PROJECT_NAME exec -T mysql-db mysql -u root -p$DB_NAME"
echo "6. Iniciar aplicação completa: docker-compose -p $PROJECT_NAME up -d"
echo ""
echo "✅ Backup concluído com sucesso!"
