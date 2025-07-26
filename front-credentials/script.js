// Gerenciador de Credenciais - JavaScript com Integração Backend

class CredentialManager {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8084/api/credentials';
        this.credentials = [];
        this.editingId = null;
        this.deleteId = null;
        this.currentCredentialForDetails = null;
        
        this.initializeEventListeners();
        this.loadCredentials();
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
            const response = await fetch(this.apiBaseUrl);
            
            if (response.ok) {
                const data = await response.json();
                this.credentials = Array.isArray(data) ? data : [data];
                this.renderCredentials();
            } else {
                this.showAlert('Erro ao carregar credenciais.', 'danger');
                this.credentials = [];
                this.renderCredentials();
            }
        } catch (error) {
            console.error('Erro ao carregar credenciais:', error);
            this.showAlert('Erro de conexão com o servidor.', 'danger');
            this.credentials = [];
            this.renderCredentials();
        } finally {
            this.showLoading(false);
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
            const response = await fetch(`${this.apiBaseUrl}/${id}`);
            
            if (response.ok) {
                const credential = await response.json();
                this.currentCredentialForDetails = credential;
                this.populateDetailsModal(credential);
                
                const modal = new bootstrap.Modal(document.getElementById('detailsModal'));
                modal.show();
            } else {
                this.showAlert('Erro ao carregar detalhes da credencial.', 'danger');
            }
        } catch (error) {
            console.error('Erro ao carregar detalhes:', error);
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
            const response = await fetch(`${this.apiBaseUrl}/${credentialId}`);
            
            if (response.ok) {
                const credential = await response.json();
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
                const response = await fetch(`${this.apiBaseUrl}/${credentialId}`);
                if (response.ok) {
                    const credential = await response.json();
                    passwordSpan.textContent = credential.password || '';
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
            const response = await fetch(`${this.apiBaseUrl}/${id}`);
            
            if (response.ok) {
                const credential = await response.json();
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
