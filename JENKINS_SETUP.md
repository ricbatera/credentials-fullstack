# Configuração do Jenkins para Credentials Full Stack

## 📋 Pré-requisitos

- Servidor Linux com Jenkins instalado
- Acesso SSH ao servidor
- Repositório público: https://github.com/ricbatera/credentials-fullstack.git

## 🚀 Passo a Passo

### 1. Preparar o Ambiente no Servidor

Execute no seu servidor Linux:

```bash
# Fazer download do script de configuração
wget https://raw.githubusercontent.com/ricbatera/credentials-fullstack/main/jenkins-setup.sh

# Dar permissão de execução
chmod +x jenkins-setup.sh

# Executar o script
./jenkins-setup.sh
```

### 2. Configurar Credenciais no Jenkins

1. Acesse o Jenkins: `http://SEU_SERVIDOR_IP:8080`
2. Vá em **Manage Jenkins** → **Manage Credentials**
3. Clique em **(global)** → **Add Credentials**

Crie as seguintes credenciais do tipo **Secret text**:

#### Credencial 1: `mysql-root-password`
- **Kind**: Secret text
- **Secret**: `credentials_root_2024!` (ou sua senha preferida)
- **ID**: `mysql-root-password`
- **Description**: MySQL Root Password

#### Credencial 2: `mysql-user-password`
- **Kind**: Secret text  
- **Secret**: `credentials_user_2024!` (ou sua senha preferida)
- **ID**: `mysql-user-password`
- **Description**: MySQL User Password

### 3. Criar o Pipeline Job

1. No Jenkins, clique em **New Item**
2. Digite o nome: `credentials-fullstack-deploy`
3. Selecione **Pipeline** e clique **OK**

### 4. Configurar o Pipeline

Na configuração do job:

#### Build Triggers (opcional)
- Marque **Poll SCM** se quiser verificação automática
- Schedule: `H/5 * * * *` (verifica a cada 5 minutos)

#### Pipeline Configuration
- **Definition**: Pipeline script from SCM
- **SCM**: Git
- **Repository URL**: `https://github.com/ricbatera/credentials-fullstack.git`
- **Branch Specifier**: `*/main`
- **Script Path**: `Jenkinsfile`

### 5. Configurar Variável de Ambiente (Opcional)

Se você quiser definir o IP do servidor automaticamente:

1. Vá em **Manage Jenkins** → **Configure System**
2. Na seção **Global Properties**, marque **Environment variables**
3. Adicione:
   - **Name**: `SERVER_IP`
   - **Value**: `SEU_IP_DO_SERVIDOR` (ex: 201.77.115.146)

### 6. Executar o Pipeline

1. Clique em **Build Now**
2. Acompanhe o progresso em **Console Output**

## 🔧 Estrutura do Pipeline

O pipeline executa os seguintes estágios:

1. **Cleanup Workspace** - Limpa o workspace
2. **Checkout** - Faz checkout do código
3. **Verify Environment** - Verifica Docker e recursos
4. **Setup Directories** - Cria diretórios necessários
5. **Create Environment File** - Cria arquivo .env
6. **Stop Existing Services** - Para serviços existentes
7. **Build and Deploy** - Constrói e sobe a stack
8. **Health Check** - Verifica saúde dos serviços

## 🌐 Acessos após Deploy

Após o deploy bem-sucedido:

- **Frontend**: `http://SEU_SERVIDOR_IP:11080`
- **Backend API**: `http://SEU_SERVIDOR_IP:11003`
- **Swagger UI**: `http://SEU_SERVIDOR_IP:11003/swagger-ui.html`
- **Actuator Health**: `http://SEU_SERVIDOR_IP:11003/actuator/health`

## 🐛 Troubleshooting

### Problema: Pipeline falha por permissões Docker
**Solução**: Execute no servidor:
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Problema: Portas em uso
**Solução**: Modifique as portas no pipeline ou pare outros serviços:
```bash
sudo netstat -tulpn | grep :11003
sudo netstat -tulpn | grep :11080
```

### Problema: Falta de espaço em disco
**Solução**: Limpe imagens Docker antigas:
```bash
docker system prune -a
```

### Problema: MySQL não inicia
**Solução**: Verifique permissões do diretório:
```bash
sudo chmod -R 777 ~/credentials/mysql-data
```

## 📝 Logs importantes

Para debugar problemas:

```bash
# Logs do Jenkins
sudo journalctl -u jenkins -f

# Logs dos containers
docker-compose -p credentials-fullstack logs -f

# Status dos containers
docker-compose -p credentials-fullstack ps
```

## 🔄 Atualizações

Para atualizar a aplicação:
1. Faça push das mudanças para o repositório
2. Execute o pipeline novamente no Jenkins
3. O pipeline irá automaticamente:
   - Fazer pull das mudanças
   - Reconstruir as imagens
   - Reiniciar os serviços

## 🛡️ Segurança

**Recomendações importantes**:

1. **Altere as senhas padrão** nas credenciais do Jenkins
2. **Configure HTTPS** no Jenkins para produção
3. **Use um proxy reverso** (Nginx) para expor apenas as portas necessárias
4. **Configure backup** do banco de dados MySQL
5. **Monitore logs** regularmente

## 📞 Suporte

Em caso de problemas:
1. Verifique os logs do pipeline
2. Confirme se Docker e Docker Compose estão funcionando
3. Verifique se as portas não estão sendo usadas por outros serviços
4. Confirme se as credenciais estão configuradas corretamente
