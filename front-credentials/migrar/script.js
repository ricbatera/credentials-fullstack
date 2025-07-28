// Gerenciador de Credenciais - JavaScript com Integração Backend

class CredentialManager {
    constructor() {
        this.apiBaseUrl = window.AppConfig?.API_BASE_URL || 'http://localhost:8084/api/credentials';
        this.consumerIdentifier = window.AppConfig?.CONSUMER_IDENTIFIER || 'frontend-app-v1';
        this.privateKey = window.AppConfig?.PRIVATE_KEY || '';
        this.publicKey = window.AppConfig?.PUBLIC_KEY || '';
        this.credentials = [];
        this.editingId = null;
        this.deleteId = null;
        this.currentCredentialForDetails = null;
        
        this.initializeEventListeners();
        this.loadCredentials();
        this.initializeRSA();
    }

    // Inicializar RSA e registrar chave pública se necessário
    async initializeRSA() {
        if (!this.privateKey || !this.publicKey) {
            console.warn('⚠️  Chaves RSA não configuradas. Usando modo compatibilidade sem criptografia.');
            console.log('📋 Para usar criptografia, configure as chaves no config.js');
            return;
        }

        try {
            console.log('🔐 Inicializando sistema de criptografia RSA...');
            console.log('🏷️  Consumer Identifier:', this.consumerIdentifier);
            
            // Verificar se o consumidor tem chave pública registrada
            const hasValidKey = await this.checkConsumerPublicKey();
            console.log('🔑 Chave pública válida registrada:', hasValidKey);
            
            if (!hasValidKey) {
                console.log('📝 Registrando nova chave pública...');
                await this.registerPublicKey();
                console.log('✅ Chave pública registrada com sucesso');
            }
            
            console.log('🎯 Sistema RSA inicializado - Backend atualizado com SENHAS REAIS!');
            console.log('📈 Melhorias: Dupla criptografia (AES + BCrypt + RSA)');
            
        } catch (error) {
            console.error('❌ Erro ao inicializar RSA:', error);
            this.showAlert('Erro ao configurar criptografia. Funcionando em modo compatibilidade.', 'warning');
        }
    }

    // Verificar se consumidor tem chave pública válida
    async checkConsumerPublicKey() {
        try {
            const response = await fetch(`${this.apiBaseUrl.replace('/credentials', '')}/consumer-keys/consumer/${this.consumerIdentifier}/valid`);
            if (response.ok) {
                const data = await response.text();
                return data === 'true';
            }
            return false;
        } catch (error) {
            console.error('Erro ao verificar chave pública:', error);
            return false;
        }
    }

    // Registrar chave pública na API
    async registerPublicKey() {
        if (!this.publicKey) {
            throw new Error('Chave pública não configurada');
        }

        const keyData = {
            consumerName: 'Frontend Credentials App',
            consumerIdentifier: this.consumerIdentifier,
            publicKey: this.publicKey,
            description: 'Chave pública para aplicação frontend de gerenciamento de credenciais'
        };

        const response = await fetch(`${this.apiBaseUrl.replace('/credentials', '')}/consumer-keys`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(keyData)
        });

        if (!response.ok) {
            throw new Error('Erro ao registrar chave pública');
        }

        console.log('Chave pública registrada com sucesso');
    }

    // Descriptografar senha usando chave privada com JSEncrypt (compatível com Java RSA PKCS1)
    async decryptPassword(encryptedPassword) {
        if (!this.privateKey || !encryptedPassword) {
            console.log('⚠️  Chave privada ou senha encriptada não disponível');
            return encryptedPassword; // Retorna como está se não tiver chave ou senha
        }

        try {
            console.log('🔐 Iniciando descriptografia com JSEncrypt...');
            console.log('📝 Senha encriptada (primeiros 50 chars):', encryptedPassword.substring(0, 50));
            console.log('📊 Tamanho da senha encriptada:', encryptedPassword.length);
            
            // Verificar se JSEncrypt está disponível
            if (typeof JSEncrypt === 'undefined') {
                throw new Error('JSEncrypt não está carregado. Verifique se a biblioteca foi incluída.');
            }
            
            // Criar instância do JSEncrypt
            const crypt = new JSEncrypt();
            
            // Definir a chave privada
            crypt.setPrivateKey(this.privateKey);
            console.log('🔑 Chave privada configurada no JSEncrypt');
            
            // Descriptografar a senha - AGORA RECEBE SENHA REAL DO BACKEND
            const decryptedPassword = crypt.decrypt(encryptedPassword);
            
            if (decryptedPassword === false) {
                throw new Error('Falha na descriptografia - senha ou chave podem estar incorretas');
            }
            
            console.log('✅ Descriptografia realizada com sucesso');
            console.log('🎯 Senha descriptografada:', decryptedPassword ? '***SENHA***' : 'null');
            console.log('📈 Status: Backend agora retorna senhas REAIS (não mais mensagens de proteção)');
            return decryptedPassword;
            
        } catch (error) {
            console.error('❌ Erro detalhado ao descriptografar senha:', error);
            console.error('📋 Stack trace:', error.stack);
            console.error('🏷️  Nome do erro:', error.name);
            console.error('💬 Mensagem do erro:', error.message);
            console.error('🔧 Dica: Verifique se a implementação RSA do backend foi atualizada corretamente');
            return encryptedPassword; // Retorna encriptado se houver erro
        }
    }

    // Método de teste para debug da descriptografia com JSEncrypt - ATUALIZADO
    async testDecryption() {
        console.log('=== 🧪 TESTE DE DESCRIPTOGRAFIA JSEncrypt (NOVA IMPLEMENTAÇÃO) ===');
        console.log('📋 Configurações atuais:');
        console.log('🌐 API Base URL:', this.apiBaseUrl);
        console.log('🏷️  Consumer Identifier:', this.consumerIdentifier);
        console.log('🔑 Tem chave privada:', !!this.privateKey);
        console.log('🔐 Tem chave pública:', !!this.publicKey);
        console.log('📚 JSEncrypt disponível:', typeof JSEncrypt !== 'undefined');
        console.log('🎯 NOVA FUNCIONALIDADE: Backend retorna senhas REAIS (não mais mensagens de proteção)');
        
        if (this.privateKey) {
            console.log('📊 Tamanho da chave privada:', this.privateKey.length);
            console.log('📝 Início da chave privada:', this.privateKey.substring(0, 50));
            
            // Validar formato da chave
            const hasCorrectHeaders = this.privateKey.includes('-----BEGIN PRIVATE KEY-----') && 
                                     this.privateKey.includes('-----END PRIVATE KEY-----');
            console.log('✅ Formato da chave privada correto:', hasCorrectHeaders);
        }
        
        // Testar com uma credencial real se existir
        if (this.credentials && this.credentials.length > 0) {
            const testCredential = this.credentials.find(c => c.encryptedPassword);
            if (testCredential) {
                console.log('🧪 Testando descriptografia com credencial real:', testCredential.nameMall);
                const result = await this.decryptPassword(testCredential.encryptedPassword);
                console.log('🎯 Resultado do teste com credencial real:', result !== testCredential.encryptedPassword ? 'SUCESSO' : 'FALHA');
                console.log('📈 Implementação: Dupla criptografia (AES interno + RSA transmissão)');
            } else {
                console.log('ℹ️  Nenhuma credencial com senha criptografada encontrada para teste');
            }
        }
        
        // Teste de conectividade com API
        try {
            const hasValidKey = await this.checkConsumerPublicKey();
            console.log('🔗 Chave pública válida na API:', hasValidKey);
        } catch (error) {
            console.error('❌ Erro ao verificar chave na API:', error.message);
        }
        
        console.log('=== ✅ FIM DO TESTE ATUALIZADO ===');
    }

    // Método adicional para testar apenas a biblioteca JSEncrypt
    testJSEncrypt() {
        console.log('=== TESTE DA BIBLIOTECA JSEncrypt ===');
        
        try {
            if (typeof JSEncrypt === 'undefined') {
                console.error('❌ JSEncrypt não está disponível');
                return false;
            }
            
            console.log('✅ JSEncrypt está disponível');
            console.log('Versão JSEncrypt:', JSEncrypt.version || 'Versão não disponível');
            
            // Testar criação de instância
            const crypt = new JSEncrypt();
            console.log('✅ Instância JSEncrypt criada com sucesso');
            
            // Testar configuração da chave
            if (this.privateKey) {
                crypt.setPrivateKey(this.privateKey);
                console.log('✅ Chave privada configurada');
                
                // Testar se a chave foi aceita
                const keySet = crypt.getPrivateKey();
                console.log('✅ Chave privada aceita pelo JSEncrypt:', !!keySet);
            }
            
            return true;
            
        } catch (error) {
            console.error('❌ Erro ao testar JSEncrypt:', error);
            return false;
        } finally {
            console.log('=== FIM TESTE JSEncrypt ===');
        }
    }

    // Converter chave privada PEM para ArrayBuffer
    parsePrivateKey(pemKey) {
        const pemHeader = '-----BEGIN PRIVATE KEY-----';
        const pemFooter = '-----END PRIVATE KEY-----';
        const pemContents = pemKey.replace(pemHeader, '').replace(pemFooter, '').replace(/\s/g, '');
        return this.base64ToArrayBuffer(pemContents);
    }

    // Converter Base64 para ArrayBuffer
    base64ToArrayBuffer(base64) {
        const binaryString = window.atob(base64);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        return bytes.buffer;
    }

    // Inicializar event listeners
    initializeEventListeners() {
        // Botão salvar credencial
        document.getElementById('saveCredential').addEventListener('click', () => {
            this.saveCredential();
        });

        // Botão toggle password
        document.getElementById('togglePassword').addEventListener('click', () => {
            this.togglePasswordVisibility('password', 'togglePassword');
        });

        // Botão toggle password nos detalhes
        document.getElementById('toggleDetailPassword').addEventListener('click', () => {
            this.toggleDetailPassword();
        });

        // Botão confirmar exclusão
        document.getElementById('confirmDelete').addEventListener('click', () => {
            this.deleteCredential();
        });

        // Botão editar dos detalhes
        document.getElementById('editFromDetails').addEventListener('click', () => {
            this.editFromDetails();
        });

        // Botões de copiar do modal de detalhes
        document.getElementById('copyDetailUsername').addEventListener('click', () => {
            const username = document.getElementById('detailUsername').textContent;
            if (username && username !== '-') {
                this.copyToClipboard(username, 'usuário');
                this.animateCopyButton(document.getElementById('copyDetailUsername'));
            }
        });

        document.getElementById('copyDetailPassword').addEventListener('click', () => {
            if (this.currentCredentialForDetails) {
                this.copyToClipboard(this.currentCredentialForDetails.password, 'senha');
                this.animateCopyButton(document.getElementById('copyDetailPassword'));
            }
        });

        document.getElementById('copyDetailId').addEventListener('click', () => {
            const id = document.getElementById('detailId').textContent;
            if (id && id !== '-') {
                this.copyToClipboard(id, 'ID');
                this.animateCopyButton(document.getElementById('copyDetailId'));
            }
        });

        // Reset do modal quando fechado
        document.getElementById('credentialModal').addEventListener('hidden.bs.modal', () => {
            this.resetModal();
        });

        // Submit do formulário
        document.getElementById('credentialForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveCredential();
        });

        // Máscara para CNPJ
        document.getElementById('cnpj').addEventListener('input', (e) => {
            this.applyCnpjMask(e.target);
        });

        // Campo de pesquisa
        document.getElementById('searchInput').addEventListener('input', (e) => {
            this.filterCredentials(e.target.value);
        });

        // Botão limpar pesquisa
        document.getElementById('clearSearch').addEventListener('click', () => {
            document.getElementById('searchInput').value = '';
            this.filterCredentials('');
        });
    }

    // Aplicar máscara CNPJ
    applyCnpjMask(input) {
        let value = input.value.replace(/\D/g, '');
        value = value.replace(/^(\d{2})(\d)/, '$1.$2');
        value = value.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
        value = value.replace(/\.(\d{3})(\d)/, '.$1/$2');
        value = value.replace(/(\d{4})(\d)/, '$1-$2');
        input.value = value;
    }

    // Filtrar credenciais
    filterCredentials(searchTerm) {
        const filteredCredentials = this.credentials.filter(credential => {
            const term = searchTerm.toLowerCase();
            return (
                credential.nameMall?.toLowerCase().includes(term) ||
                credential.cnpj?.toLowerCase().includes(term) ||
                credential.urlPortal?.toLowerCase().includes(term) ||
                credential.username?.toLowerCase().includes(term)
            );
        });
        this.renderCredentials(filteredCredentials);
    }

    // Carregar credenciais da API
    async loadCredentials() {
        try {
            this.showLoading(true);
            let response;
            
            // Se tiver chaves configuradas, usar endpoint com senhas criptografadas
            if (this.privateKey && this.consumerIdentifier) {
                const encryptedUrl = `${this.apiBaseUrl}/encrypted/${this.consumerIdentifier}`;
                console.log('🔐 Carregando credenciais com criptografia:', encryptedUrl);
                
                response = await fetch(encryptedUrl);
                
                if (response.ok) {
                    const data = await response.json();
                    console.log('✅ Dados recebidos com criptografia:', data.length || 1, 'credenciais');
                    this.credentials = Array.isArray(data) ? data : [data];
                    
                    // Descriptografar senhas - NOVA IMPLEMENTAÇÃO: senha real criptografada
                    for (let credential of this.credentials) {
                        if (credential.encryptedPassword) {
                            console.log(`🔓 Descriptografando senha para credencial ID: ${credential.id}`);
                            credential.password = await this.decryptPassword(credential.encryptedPassword);
                            console.log(`✅ Senha descriptografada com sucesso para: ${credential.nameMall}`);
                        }
                    }
                    
                    this.renderCredentials();
                } else {
                    // Melhor tratamento de erros baseado na documentação
                    const errorText = await response.text();
                    console.error(`❌ Erro ao carregar com criptografia (${response.status}):`, errorText);
                    
                    if (response.status === 400) {
                        // Fallback para endpoint sem criptografia se consumidor não tiver chave válida
                        console.warn('⚠️  Consumidor sem chave pública válida, usando endpoint sem criptografia');
                        await this.loadCredentialsWithoutEncryption();
                    } else if (response.status === 500) {
                        // Novo tratamento para erros internos do servidor
                        console.error('💥 Erro interno do servidor. Verificando se é problema de implementação...');
                        this.showAlert('Erro interno do servidor. Tentando modo compatibilidade...', 'warning');
                        await this.loadCredentialsWithoutEncryption();
                    } else {
                        this.showAlert('Erro ao carregar credenciais.', 'danger');
                        this.credentials = [];
                        this.renderCredentials();
                    }
                }
            } else {
                // Usar endpoint sem criptografia
                console.log('📝 Modo compatibilidade - carregando sem criptografia');
                await this.loadCredentialsWithoutEncryption();
            }
        } catch (error) {
            console.error('💥 Erro crítico ao carregar credenciais:', error);
            this.showAlert('Erro de conexão com o servidor.', 'danger');
            this.credentials = [];
            this.renderCredentials();
        } finally {
            this.showLoading(false);
        }
    }

    // Carregar credenciais sem criptografia (fallback)
    async loadCredentialsWithoutEncryption() {
        console.log('📝 Carregando credenciais em modo compatibilidade (sem criptografia)');
        const response = await fetch(this.apiBaseUrl);
        
        if (response.ok) {
            const data = await response.json();
            this.credentials = Array.isArray(data) ? data : [data];
            console.log('✅ Credenciais carregadas em modo compatibilidade:', this.credentials.length);
            
            // No modo compatibilidade, as senhas não são retornadas por segurança
            this.credentials.forEach(credential => {
                if (!credential.password) {
                    credential.password = '***PROTEGIDA***';
                }
            });
            
            this.renderCredentials();
        } else {
            console.error('❌ Erro ao carregar credenciais no modo compatibilidade');
            this.showAlert('Erro ao carregar credenciais.', 'danger');
            this.credentials = [];
            this.renderCredentials();
        }
    }

    // Renderizar tabela de credenciais
    renderCredentials(credentialsToRender = null) {
        const tbody = document.getElementById('credentialsTableBody');
        const emptyMessage = document.getElementById('emptyMessage');
        
        tbody.innerHTML = '';

        const credentials = credentialsToRender || this.credentials;

        if (credentials.length === 0) {
            emptyMessage.classList.remove('d-none');
            return;
        }

        emptyMessage.classList.add('d-none');

        credentials.forEach(credential => {
            const row = this.createCredentialRow(credential);
            tbody.appendChild(row);
        });
    }

    // Criar linha da tabela
    createCredentialRow(credential) {
        const tr = document.createElement('tr');
        tr.className = 'credential-row';
        
        // Extrair domínio da URL para exibição mais limpa
        const domain = this.extractDomain(credential.urlPortal);
        
        tr.innerHTML = `
            <td>
                <span class="fw-medium text-primary">
                    <i class="bi bi-shop"></i>
                    ${this.escapeHtml(credential.nameMall)}
                </span>
            </td>
            <td class="url-cell">
                <a href="${credential.urlPortal}" target="_blank" class="url-link" title="${credential.urlPortal}">
                    <i class="bi bi-globe"></i>
                    ${domain}
                </a>
            </td>
            <td>
                <span class="fw-medium">${this.escapeHtml(credential.username)}</span>
                <button class="btn btn-sm btn-outline-secondary ms-2" onclick="credentialManager.copyToClipboard('${this.escapeHtml(credential.username)}', 'usuário')" title="Copiar usuário">
                    <i class="bi bi-files"></i>
                </button>
            </td>
            <td class="password-cell">
                <span class="password-hidden">
                    ${'•'.repeat(8)}
                </span>
                <button class="btn btn-sm btn-outline-secondary ms-2" onclick="credentialManager.togglePasswordInTable(this, '${credential.id}')" title="Mostrar/Ocultar senha">
                    <i class="bi bi-eye"></i>
                </button>
                <button class="btn btn-sm btn-outline-secondary ms-1" onclick="credentialManager.copyPasswordToClipboard('${credential.id}')" title="Copiar senha">
                    <i class="bi bi-files"></i>
                </button>
            </td>
            <td class="text-center">
                <div class="action-buttons">
                    <button class="btn btn-sm btn-info me-1" onclick="credentialManager.showDetails('${credential.id}')" title="Detalhes">
                        <i class="bi bi-info-circle"></i>
                    </button>
                    <button class="btn btn-sm btn-edit me-1" onclick="credentialManager.editCredential('${credential.id}')" title="Editar">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-delete" onclick="credentialManager.confirmDeleteCredential('${credential.id}')" title="Excluir">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        `;

        return tr;
    }

    // Extrair domínio da URL
    extractDomain(url) {
        try {
            const urlObj = new URL(url);
            return urlObj.hostname;
        } catch {
            return url;
        }
    }

    // Escapar HTML para segurança
    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // Mostrar detalhes da credencial
    async showDetails(id) {
        try {
            this.showLoading(true);
            let response;
            let credential;
            
            // Se tiver chaves configuradas, tentar usar endpoint com criptografia
            if (this.privateKey && this.consumerIdentifier) {
                const encryptedUrl = `${this.apiBaseUrl}/${id}/encrypted/${this.consumerIdentifier}`;
                console.log(`🔐 Buscando detalhes com criptografia para ID: ${id}`);
                
                response = await fetch(encryptedUrl);
                
                if (response.ok) {
                    credential = await response.json();
                    console.log('✅ Credencial recebida com criptografia:', credential.nameMall);
                    
                    // Descriptografar senha - NOVA IMPLEMENTAÇÃO: senha real
                    if (credential.encryptedPassword) {
                        console.log('🔓 Descriptografando senha individual...');
                        credential.password = await this.decryptPassword(credential.encryptedPassword);
                        console.log('✅ Senha descriptografada para detalhes');
                    }
                } else {
                    // Melhor tratamento de erro baseado na nova implementação
                    const errorText = await response.text();
                    console.error(`❌ Erro detalhes com criptografia (${response.status}):`, errorText);
                    
                    if (response.status === 400) {
                        // Fallback para endpoint sem criptografia
                        console.warn('⚠️  Fallback para endpoint sem criptografia');
                        response = await fetch(`${this.apiBaseUrl}/${id}`);
                        if (response.ok) {
                            credential = await response.json();
                        }
                    } else if (response.status === 500) {
                        // Novo tratamento para erros internos
                        console.error('💥 Erro interno do servidor ao buscar detalhes');
                        this.showAlert('Erro interno ao carregar detalhes. Tentando modo compatibilidade...', 'warning');
                        response = await fetch(`${this.apiBaseUrl}/${id}`);
                        if (response.ok) {
                            credential = await response.json();
                        }
                    }
                }
            } else {
                // Usar endpoint sem criptografia
                console.log(`📝 Buscando detalhes sem criptografia para ID: ${id}`);
                response = await fetch(`${this.apiBaseUrl}/${id}`);
                if (response.ok) {
                    credential = await response.json();
                }
            }
            
            if (credential) {
                this.currentCredentialForDetails = credential;
                this.populateDetailsModal(credential);
                
                const modal = new bootstrap.Modal(document.getElementById('detailsModal'));
                modal.show();
            } else {
                this.showAlert('Erro ao carregar detalhes da credencial.', 'danger');
            }
        } catch (error) {
            console.error('💥 Erro crítico ao carregar detalhes:', error);
            this.showAlert('Erro de conexão com o servidor.', 'danger');
        } finally {
            this.showLoading(false);
        }
    }

    // Preencher modal de detalhes
    populateDetailsModal(credential) {
        document.getElementById('detailShoppingName').textContent = credential.nameMall || '-';
        document.getElementById('detailCnpj').textContent = credential.cnpj || '-';
        document.getElementById('detailUrl').href = credential.urlPortal || '#';
        document.getElementById('detailUrl').textContent = credential.urlPortal || '-';
        document.getElementById('detailUsername').textContent = credential.username || '-';
        
        const statusBadge = document.getElementById('detailStatus');
        if (credential.active) {
            statusBadge.className = 'badge bg-success';
            statusBadge.textContent = 'Ativo';
        } else {
            statusBadge.className = 'badge bg-danger';
            statusBadge.textContent = 'Inativo';
        }
        
        document.getElementById('detailId').textContent = credential.id || '-';
        document.getElementById('detailCreatedAt').textContent = this.formatDate(credential.createdAt);
        document.getElementById('detailUpdatedAt').textContent = this.formatDate(credential.updatedAt);
        
        // Reset do password visibility
        const passwordSpan = document.getElementById('detailPassword');
        const toggleBtn = document.getElementById('toggleDetailPassword');
        passwordSpan.textContent = '••••••••';
        passwordSpan.className = 'password-hidden';
        passwordSpan.setAttribute('data-password', credential.password || '');
        toggleBtn.querySelector('i').className = 'bi bi-eye';
    }

    // Formattar data
    formatDate(dateString) {
        if (!dateString) return '-';
        try {
            return new Date(dateString).toLocaleString('pt-BR');
        } catch {
            return '-';
        }
    }

    // Toggle password nos detalhes
    toggleDetailPassword() {
        const passwordSpan = document.getElementById('detailPassword');
        const toggleButton = document.getElementById('toggleDetailPassword');
        const icon = toggleButton.querySelector('i');
        const actualPassword = passwordSpan.getAttribute('data-password');
        
        if (passwordSpan.classList.contains('password-hidden')) {
            passwordSpan.textContent = actualPassword;
            passwordSpan.classList.remove('password-hidden');
            icon.className = 'bi bi-eye-slash';
        } else {
            passwordSpan.textContent = '••••••••';
            passwordSpan.classList.add('password-hidden');
            icon.className = 'bi bi-eye';
        }
    }

    // Editar a partir dos detalhes
    editFromDetails() {
        if (this.currentCredentialForDetails) {
            // Fechar modal de detalhes
            const detailsModal = bootstrap.Modal.getInstance(document.getElementById('detailsModal'));
            detailsModal.hide();
            
            // Abrir modal de edição
            setTimeout(() => {
                this.editCredential(this.currentCredentialForDetails.id);
            }, 300);
        }
    }

    // Copiar texto para área de transferência
    async copyToClipboard(text, type = 'texto') {
        try {
            await navigator.clipboard.writeText(text);
            this.showAlert(`${type.charAt(0).toUpperCase() + type.slice(1)} copiado para a área de transferência!`, 'success');
            
            // Feedback visual - encontrar e animar o botão
            const buttons = document.querySelectorAll('.btn-outline-secondary');
            buttons.forEach(btn => {
                if (btn.onclick && btn.onclick.toString().includes(text.replace(/'/g, "\\'"))) {
                    this.animateCopyButton(btn);
                }
            });
        } catch (error) {
            console.error('Erro ao copiar para área de transferência:', error);
            
            // Fallback para navegadores mais antigos
            this.fallbackCopyToClipboard(text, type);
        }
    }

    // Copiar senha para área de transferência (busca da API)
    async copyPasswordToClipboard(credentialId) {
        try {
            this.showLoading(true);
            let response;
            let credential;
            
            // Se tiver chaves configuradas, tentar usar endpoint com criptografia
            if (this.privateKey && this.consumerIdentifier) {
                const encryptedUrl = `${this.apiBaseUrl}/${credentialId}/encrypted/${this.consumerIdentifier}`;
                response = await fetch(encryptedUrl);
                
                if (response.ok) {
                    credential = await response.json();
                    // Descriptografar senha
                    if (credential.encryptedPassword) {
                        credential.password = await this.decryptPassword(credential.encryptedPassword);
                    }
                } else if (response.status === 400) {
                    // Fallback para endpoint sem criptografia
                    response = await fetch(`${this.apiBaseUrl}/${credentialId}`);
                    if (response.ok) {
                        credential = await response.json();
                    }
                }
            } else {
                // Usar endpoint sem criptografia
                response = await fetch(`${this.apiBaseUrl}/${credentialId}`);
                if (response.ok) {
                    credential = await response.json();
                }
            }
            
            if (credential && credential.password) {
                await this.copyToClipboard(credential.password, 'senha');
            } else {
                this.showAlert('Erro ao obter senha para cópia.', 'danger');
            }
        } catch (error) {
            console.error('Erro ao copiar senha:', error);
            this.showAlert('Erro de conexão ao copiar senha.', 'danger');
        } finally {
            this.showLoading(false);
        }
    }

    // Fallback para copiar em navegadores antigos
    fallbackCopyToClipboard(text, type) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        
        try {
            document.execCommand('copy');
            this.showAlert(`${type.charAt(0).toUpperCase() + type.slice(1)} copiado para a área de transferência!`, 'success');
        } catch (err) {
            console.error('Fallback copy failed:', err);
            this.showAlert('Não foi possível copiar. Tente selecionar e copiar manualmente.', 'warning');
        }
        
        document.body.removeChild(textArea);
    }

    // Animação visual do botão de copiar
    animateCopyButton(button) {
        const icon = button.querySelector('i');
        const originalClass = icon.className;
        
        // Mudar ícone temporariamente
        icon.className = 'bi bi-check-lg text-success';
        button.classList.add('border-success');
        
        // Reverter após 1 segundo
        setTimeout(() => {
            icon.className = originalClass;
            button.classList.remove('border-success');
        }, 1000);
    }

    // Toggle visibilidade da senha na tabela
    async togglePasswordInTable(button, credentialId) {
        const passwordSpan = button.previousElementSibling;
        const icon = button.querySelector('i');
        
        if (passwordSpan.classList.contains('password-hidden')) {
            // Buscar a senha real da API
            try {
                this.showLoading(true);
                let response;
                let credential;
                
                // Se tiver chaves configuradas, tentar usar endpoint com criptografia
                if (this.privateKey && this.consumerIdentifier) {
                    const encryptedUrl = `${this.apiBaseUrl}/${credentialId}/encrypted/${this.consumerIdentifier}`;
                    response = await fetch(encryptedUrl);
                    
                    if (response.ok) {
                        credential = await response.json();
                        // Descriptografar senha
                        if (credential.encryptedPassword) {
                            credential.password = await this.decryptPassword(credential.encryptedPassword);
                        }
                    } else if (response.status === 400) {
                        // Fallback para endpoint sem criptografia
                        response = await fetch(`${this.apiBaseUrl}/${credentialId}`);
                        if (response.ok) {
                            credential = await response.json();
                        }
                    }
                } else {
                    // Usar endpoint sem criptografia
                    response = await fetch(`${this.apiBaseUrl}/${credentialId}`);
                    if (response.ok) {
                        credential = await response.json();
                    }
                }
                
                if (credential && credential.password) {
                    passwordSpan.textContent = credential.password;
                    passwordSpan.classList.remove('password-hidden');
                    icon.className = 'bi bi-eye-slash';
                    button.title = 'Ocultar senha';
                } else {
                    this.showAlert('Erro ao carregar senha.', 'danger');
                }
            } catch (error) {
                console.error('Erro ao carregar senha:', error);
                this.showAlert('Erro de conexão.', 'danger');
            } finally {
                this.showLoading(false);
            }
        } else {
            passwordSpan.textContent = '••••••••';
            passwordSpan.classList.add('password-hidden');
            icon.className = 'bi bi-eye';
            button.title = 'Mostrar senha';
        }
    }

    // Toggle visibilidade da senha no modal
    togglePasswordVisibility(inputId, buttonId) {
        const passwordInput = document.getElementById(inputId);
        const toggleButton = document.getElementById(buttonId);
        const icon = toggleButton.querySelector('i');

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            icon.className = 'bi bi-eye-slash';
        } else {
            passwordInput.type = 'password';
            icon.className = 'bi bi-eye';
        }
    }

    // Salvar credencial
    async saveCredential() {
        const form = document.getElementById('credentialForm');
        
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const shoppingName = document.getElementById('shoppingName').value.trim();
        const cnpj = document.getElementById('cnpj').value.trim();
        const url = document.getElementById('url').value.trim();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const credentialId = document.getElementById('credentialId').value;

        // Validações adicionais
        if (!this.isValidUrl(url)) {
            this.showAlert('Por favor, insira uma URL válida.', 'danger');
            return;
        }

        if (password.length < 1) {
            this.showAlert('A senha não pode estar vazia.', 'danger');
            return;
        }

        if (shoppingName.length < 2) {
            this.showAlert('O nome do shopping deve ter pelo menos 2 caracteres.', 'danger');
            return;
        }

        const credentialData = {
            nameMall: shoppingName,
            cnpj: cnpj || null,
            urlPortal: url,
            username: username,
            password: password,
            active: true
        };

        try {
            this.showLoading(true);
            let response;
            
            if (credentialId) {
                // Editando credencial existente
                response = await fetch(`${this.apiBaseUrl}/${credentialId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(credentialData)
                });
                
                if (response.ok) {
                    this.showAlert('Credencial atualizada com sucesso!', 'success');
                } else {
                    const error = await response.text();
                    this.showAlert(`Erro ao atualizar credencial: ${error}`, 'danger');
                    return;
                }
            } else {
                // Adicionando nova credencial
                response = await fetch(this.apiBaseUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(credentialData)
                });
                
                if (response.ok) {
                    this.showAlert('Credencial adicionada com sucesso!', 'success');
                } else {
                    const error = await response.text();
                    this.showAlert(`Erro ao adicionar credencial: ${error}`, 'danger');
                    return;
                }
            }

            // Recarregar a lista
            await this.loadCredentials();
            
            // Fechar modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('credentialModal'));
            modal.hide();
            
        } catch (error) {
            console.error('Erro ao salvar credencial:', error);
            this.showAlert('Erro de conexão com o servidor.', 'danger');
        } finally {
            this.showLoading(false);
        }
    }

    // Validar URL
    isValidUrl(string) {
        try {
            new URL(string);
            return true;
        } catch {
            return false;
        }
    }

    // Editar credencial
    async editCredential(id) {
        try {
            this.showLoading(true);
            let response;
            let credential;
            
            // Se tiver chaves configuradas, tentar usar endpoint com criptografia
            if (this.privateKey && this.consumerIdentifier) {
                const encryptedUrl = `${this.apiBaseUrl}/${id}/encrypted/${this.consumerIdentifier}`;
                response = await fetch(encryptedUrl);
                
                if (response.ok) {
                    credential = await response.json();
                    // Descriptografar senha
                    if (credential.encryptedPassword) {
                        credential.password = await this.decryptPassword(credential.encryptedPassword);
                    }
                } else if (response.status === 400) {
                    // Fallback para endpoint sem criptografia
                    response = await fetch(`${this.apiBaseUrl}/${id}`);
                    if (response.ok) {
                        credential = await response.json();
                    }
                }
            } else {
                // Usar endpoint sem criptografia
                response = await fetch(`${this.apiBaseUrl}/${id}`);
                if (response.ok) {
                    credential = await response.json();
                }
            }
            
            if (credential) {
                this.editingId = id;

                // Preencher formulário
                document.getElementById('credentialId').value = credential.id;
                document.getElementById('shoppingName').value = credential.nameMall || '';
                document.getElementById('cnpj').value = credential.cnpj || '';
                document.getElementById('url').value = credential.urlPortal || '';
                document.getElementById('username').value = credential.username || '';
                document.getElementById('password').value = credential.password || '';

                // Atualizar título do modal
                document.getElementById('credentialModalLabel').innerHTML = `
                    <i class="bi bi-pencil"></i>
                    Editar Credencial
                `;

                // Abrir modal
                const modal = new bootstrap.Modal(document.getElementById('credentialModal'));
                modal.show();
            } else {
                this.showAlert('Erro ao carregar credencial para edição.', 'danger');
            }
        } catch (error) {
            console.error('Erro ao carregar credencial:', error);
            this.showAlert('Erro de conexão com o servidor.', 'danger');
        } finally {
            this.showLoading(false);
        }
    }

    // Confirmar exclusão
    confirmDeleteCredential(id) {
        this.deleteId = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    }

    // Excluir credencial
    async deleteCredential() {
        if (!this.deleteId) return;

        try {
            this.showLoading(true);
            const response = await fetch(`${this.apiBaseUrl}/${this.deleteId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.showAlert('Credencial excluída com sucesso!', 'success');
                await this.loadCredentials();
            } else {
                const error = await response.text();
                this.showAlert(`Erro ao excluir credencial: ${error}`, 'danger');
            }
        } catch (error) {
            console.error('Erro ao excluir credencial:', error);
            this.showAlert('Erro de conexão com o servidor.', 'danger');
        } finally {
            this.showLoading(false);
            this.deleteId = null;
            
            // Fechar modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
            modal.hide();
        }
    }

    // Reset do modal
    resetModal() {
        document.getElementById('credentialForm').reset();
        document.getElementById('credentialId').value = '';
        this.editingId = null;

        // Resetar título do modal
        document.getElementById('credentialModalLabel').innerHTML = `
            <i class="bi bi-plus-circle"></i>
            Adicionar Nova Credencial
        `;

        // Resetar tipo do input de senha
        const passwordInput = document.getElementById('password');
        const toggleButton = document.getElementById('togglePassword');
        passwordInput.type = 'password';
        toggleButton.querySelector('i').className = 'bi bi-eye';
    }

    // Mostrar loading
    showLoading(show) {
        const container = document.querySelector('.container');
        if (show) {
            container.classList.add('loading');
        } else {
            container.classList.remove('loading');
        }
    }

    // Mostrar alerta
    showAlert(message, type = 'info') {
        // Remove alertas existentes
        const existingAlerts = document.querySelectorAll('.alert');
        existingAlerts.forEach(alert => alert.remove());

        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        document.body.appendChild(alertDiv);

        // Auto remove após 5 segundos
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }
}

// Inicializar aplicação quando DOM estiver carregado
document.addEventListener('DOMContentLoaded', () => {
    window.credentialManager = new CredentialManager();
    
    // Adicionar funções de teste globais para debug
    window.testDecryptFunction = async () => {
        console.log('Executando teste de descriptografia JSEncrypt...');
        if (window.credentialManager) {
            return await window.credentialManager.testDecryption();
        } else {
            console.error('CredentialManager não inicializado');
        }
    };
    
    window.testJSEncrypt = () => {
        console.log('Testando biblioteca JSEncrypt...');
        if (window.credentialManager) {
            return window.credentialManager.testJSEncrypt();
        } else {
            console.error('CredentialManager não inicializado');
        }
    };
    
    // Função para testar descriptografia simples
    window.quickDecryptTest = (encryptedPassword) => {
        console.log('Teste rápido de descriptografia...');
        if (window.credentialManager) {
            return window.credentialManager.decryptPassword(encryptedPassword || "OUmCMb4r7Lkh3aBZ839o+mm03NRpXf0toiLfEv55N95DQmQBfvIR6+Xfcif0Jn0vXQb3Gdr4NejtysJS2Cm6YPND6Q10wDihzaecZ+FLaQtzwcehH2jIShQRExxmeOHiTA4QfEOPA4tU6PT58pvZ9XxhFORRgyv7xnCLhNHpRbAIj7yL7yQn98FmkMYkc5y8PISdxi9hp2gHaTV3Xc0NKB+VqoqiZk5WgLPavuT2thTG/1kBoif1M8bDkvv50ZpR5Bjr2H0nOSFg7j3fuE1T/wCnCY9ESatWkGmRrzt41iQJwdEw7wNXBTgNfwUumxCj4rVnxjXCP+VTdzwQu8dk1w==");
        } else {
            console.error('CredentialManager não inicializado');
        }
    };
});

// Teclas de atalho
document.addEventListener('keydown', (e) => {
    // Ctrl + N para nova credencial
    if (e.ctrlKey && e.key === 'n') {
        e.preventDefault();
        const modal = new bootstrap.Modal(document.getElementById('credentialModal'));
        modal.show();
    }
    
    // Escape para fechar modais
    if (e.key === 'Escape') {
        const modals = document.querySelectorAll('.modal.show');
        modals.forEach(modal => {
            const modalInstance = bootstrap.Modal.getInstance(modal);
            if (modalInstance) {
                modalInstance.hide();
            }
        });
    }
});
