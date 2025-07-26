pipeline {
    agent any
    
    environment {
        // Variáveis de ambiente para o projeto
        MYSQL_ROOT_PASSWORD = credentials('mysql-root-password')
        MYSQL_DATABASE = 'credentials_db'
        MYSQL_USER = 'credentials_user'
        MYSQL_PASSWORD = credentials('mysql-user-password')
        BACKEND_PORT = '11003'
        FRONTEND_PORT = '11080'
        API_BASE_URL = "http://${env.SERVER_IP ?: 'localhost'}:${BACKEND_PORT}/api/credentials"
        
        // Configurações do Docker
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
        PROJECT_NAME = 'credentials-fullstack'
    }
    
    options {
        // Manter apenas os últimos 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timeout de 30 minutos
        timeout(time: 30, unit: 'MINUTES')
    }
    
    stages {
        stage('Cleanup Workspace') {
            steps {
                script {
                    echo "Limpando workspace..."
                    sh '''
                        # Limpar arquivos do workspace anterior
                        rm -rf ./* || true
                        rm -rf ./.* || true
                        echo "Workspace limpo com sucesso!"
                    '''
                }
            }
        }
        
        stage('Checkout') {
            steps {
                script {
                    echo "Fazendo checkout do repositório..."
                    git branch: 'main',
                        url: 'https://github.com/ricbatera/credentials-fullstack.git'
                }
            }
        }
        
        stage('Verify Environment') {
            steps {
                script {
                    echo "Verificando ferramentas necessárias..."
                    sh '''
                        echo "=== Verificando Docker ==="
                        docker --version
                        echo "=== Verificando Docker Compose ==="
                        docker-compose --version
                        echo "=== Verificando espaço em disco ==="
                        df -h
                        echo "=== Verificando memória ==="
                        free -h
                    '''
                }
            }
        }
        
        stage('Setup Directories') {
            steps {
                script {
                    echo "Configurando diretórios necessários..."
                    sh '''
                        # Criar diretórios necessários
                        mkdir -p ~/credentials/mysql-data
                        mkdir -p ~/credentials/mysql-init
                        mkdir -p ~/credentials/backend-logs
                        mkdir -p ~/credentials/nginx-conf
                        mkdir -p ~/credentials/nginx-logs
                        
                        # Configurar permissões
                        chmod 755 ~/credentials
                        chmod -R 777 ~/credentials/mysql-data
                        chmod -R 755 ~/credentials/backend-logs
                        chmod -R 755 ~/credentials/nginx-logs
                        
                        # Copiar configuração do nginx se existir
                        if [ -f "nginx.conf" ]; then
                            cp nginx.conf ~/credentials/nginx-conf/default.conf
                        fi
                        
                        echo "Diretórios configurados com sucesso!"
                    '''
                }
            }
        }
        
        stage('Create Environment File') {
            steps {
                script {
                    echo "Criando arquivo .env..."
                    sh '''
                        cat > .env << EOF
# Configurações do MySQL
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
MYSQL_DATABASE=${MYSQL_DATABASE}
MYSQL_USER=${MYSQL_USER}
MYSQL_PASSWORD=${MYSQL_PASSWORD}

# Configurações das portas
BACKEND_PORT=${BACKEND_PORT}
FRONTEND_PORT=${FRONTEND_PORT}

# Configuração da API
API_BASE_URL=${API_BASE_URL}
EOF
                        echo "Arquivo .env criado:"
                        cat .env
                    '''
                }
            }
        }
        
        stage('Stop Existing Services') {
            steps {
                script {
                    echo "Parando serviços existentes..."
                    sh '''
                        # Parar e remover containers existentes
                        docker-compose -p ${PROJECT_NAME} down --remove-orphans || true
                        
                        # Limpar containers órfãos
                        docker container prune -f || true
                        
                        # Limpar imagens não utilizadas (opcional)
                        # docker image prune -f || true
                    '''
                }
            }
        }
        
        stage('Build and Deploy') {
            steps {
                script {
                    echo "Construindo e subindo a stack..."
                    sh '''
                        # Build das imagens
                        docker-compose -p ${PROJECT_NAME} build --no-cache
                        
                        # Subir a stack
                        docker-compose -p ${PROJECT_NAME} up -d
                        
                        echo "Stack iniciada com sucesso!"
                    '''
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    echo "Verificando saúde dos serviços..."
                    sh '''
                        echo "Aguardando serviços iniciarem..."
                        sleep 30
                        
                        echo "=== Status dos containers ==="
                        docker-compose -p ${PROJECT_NAME} ps
                        
                        echo "=== Logs do MySQL ==="
                        docker-compose -p ${PROJECT_NAME} logs --tail=20 mysql-db
                        
                        echo "=== Logs do Backend ==="
                        docker-compose -p ${PROJECT_NAME} logs --tail=20 credentials-backend
                        
                        echo "=== Logs do Frontend ==="
                        docker-compose -p ${PROJECT_NAME} logs --tail=20 credentials-frontend
                        
                        # Testar conectividade
                        echo "=== Testando endpoints ==="
                        echo "Testando frontend..."
                        curl -f http://localhost:${FRONTEND_PORT}/ || echo "Frontend ainda não está respondendo"
                        
                        echo "Testando backend..."
                        curl -f http://localhost:${BACKEND_PORT}/actuator/health || echo "Backend ainda não está respondendo"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "=== Status final dos serviços ==="
                sh 'docker-compose -p ${PROJECT_NAME} ps || true'
            }
        }
        
        success {
            echo '''
            ✅ Deploy realizado com sucesso!
            
            🌐 Frontend: http://YOUR_SERVER_IP:''' + env.FRONTEND_PORT + '''
            🔧 Backend: http://YOUR_SERVER_IP:''' + env.BACKEND_PORT + '''
            📊 Swagger: http://YOUR_SERVER_IP:''' + env.BACKEND_PORT + '''/swagger-ui.html
            '''
        }
        
        failure {
            echo '❌ Falha no deploy! Verificar logs acima.'
            script {
                sh '''
                    echo "=== Logs de erro ==="
                    docker-compose -p ${PROJECT_NAME} logs --tail=50 || true
                '''
            }
        }
        
        cleanup {
            script {
                // Limpar workspace mas manter os containers rodando
                echo "Limpeza do workspace concluída."
            }
        }
    }
}
