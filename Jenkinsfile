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
                        
                        # Configurar permissões básicas apenas se os diretórios estiverem vazios
                        chmod 755 ~/credentials
                        
                        # Configurar permissões apenas se não houver dados do MySQL existentes
                        if [ ! -f "~/credentials/mysql-data/ibdata1" ]; then
                            chmod -R 777 ~/credentials/mysql-data || echo "Aviso: Não foi possível alterar permissões do mysql-data (pode já estar em uso)"
                        else
                            echo "Dados do MySQL já existem, mantendo permissões atuais"
                        fi
                        
                        chmod -R 755 ~/credentials/backend-logs || true
                        chmod -R 755 ~/credentials/nginx-logs || true
                        
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
        
        stage('Backend Tests and Build') {
            steps {
                script {
                    echo "Testando e compilando o backend Java..."
                    sh '''
                        cd credential-portals-service
                        
                        echo "=== Informações do Java ==="
                        java -version
                        
                        echo "=== Informações do ambiente ==="
                        locale
                        echo "LANG: ${LANG:-'not set'}"
                        echo "LC_ALL: ${LC_ALL:-'not set'}"
                        
                        echo "=== Verificando estrutura do projeto ==="
                        ls -la
                        echo "Verificando DTOs específicos..."
                        ls -la src/main/java/br/com/consultdg/credential_portals_service/api/model/
                        
                        echo "Verificando imports no Controller..."
                        grep -n "import.*CredentialsWithEncryptedPasswordDTO" src/main/java/br/com/consultdg/credential_portals_service/api/controller/CredentialsController.java || true
                        grep -n "import.*PasswordVerificationRequestDTO" src/main/java/br/com/consultdg/credential_portals_service/api/controller/CredentialsController.java || true
                        
                        echo "=== Configurando encoding ==="
                        export LANG=C.UTF-8
                        export LC_ALL=C.UTF-8
                        export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
                        
                        echo "=== Compilando projeto ==="
                        chmod +x mvnw
                        ./mvnw clean compile -e -X 2>&1 | tee compile.log
                        
                        echo "=== Verificando erros de compilação ==="
                        if grep -i "error" compile.log; then
                            echo "Erros encontrados na compilação:"
                            grep -i "error" compile.log
                            echo "Contexto dos erros:"
                            grep -B 5 -A 5 -i "error" compile.log
                        fi
                        
                        echo "=== Executando testes ==="
                        ./mvnw test -e
                        
                        echo "=== Gerando JAR ==="
                        ./mvnw package -DskipTests -e
                        
                        ls -la target/
                    '''
                }
            }
        }

        stage('Stop Existing Services') {
            steps {
                script {
                    echo "Parando serviços existentes..."
                    sh '''
                        # Parar e remover containers existentes do projeto específico
                        docker-compose -p ${PROJECT_NAME} down --remove-orphans || true
                        
                        # Aguardar um pouco para garantir que os containers pararam
                        sleep 5
                        
                        # Verificar se ainda há containers rodando
                        RUNNING_CONTAINERS=$(docker ps -q --filter "name=${PROJECT_NAME}" | wc -l)
                        if [ "$RUNNING_CONTAINERS" -gt 0 ]; then
                            echo "Ainda há containers rodando, forçando parada..."
                            docker ps --filter "name=${PROJECT_NAME}" --format "table {{.Names}}\t{{.Status}}"
                            docker stop $(docker ps -q --filter "name=${PROJECT_NAME}") || true
                            docker rm $(docker ps -aq --filter "name=${PROJECT_NAME}") || true
                        fi
                        
                        # Limpar containers órfãos (apenas se necessário)
                        docker container prune -f || true
                        
                        echo "Serviços parados com sucesso!"
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
                        docker-compose -p ${PROJECT_NAME} up -d --build
                        
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
