import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ApiService } from '../../services/api.service';
import { ConfigService, AppConfig as IAppConfig } from '../../services/config.service';
import { CredentialResponse, CredentialRequest, CredentialsWithEncryptedPasswordResponse } from '../../interfaces';

// Interface local para estender a interface base com propriedades específicas do componente
export interface CredentialWithUIState extends CredentialResponse {
  showPassword?: boolean;
  decryptedPassword?: string;
  passwordOfInvoice?: string; // Adicionado para armazenar a senha do boleto
}

export interface CredentialFormData {
  nameMall: string;
  urlPortal: string;
  username: string;
  password: string;
  passwordOfInvoice?: string;
  cnpj?: string;
  active: boolean;
}

@Component({
  selector: 'app-credentials',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './credentials.html',
  styleUrl: './credentials.css'
})
export class Credentials implements OnInit {
  // Configuração via serviço
  private config: IAppConfig;

  // Estados dos dados
  credentials: CredentialWithUIState[] = [];
  filteredCredentials: CredentialWithUIState[] = [];
  selectedCredential: CredentialWithUIState | null = null;
  editingCredential: CredentialWithUIState | null = null;
  deleteId: string | null = null;

  // Estados dos modais
  showCredentialModal = false;
  showDeleteModal = false;
  showDetailsModal = false;

  // Estados dos formulários
  credentialFormData: CredentialFormData = {
    nameMall: '',
    urlPortal: '',
    username: '',
    password: '',
    cnpj: '',
    active: true
  };

  // Estados de interface
  searchTerm = '';
  showPassword = false;
  showDetailPassword = false;
  isLoading = false;

  // Estados de alertas
  showAlert = false;
  alertMessage = '';
  alertType: 'success' | 'error' | 'info' | 'warning' = 'info';

  constructor(
    private http: HttpClient,
    private apiService: ApiService,
    private configService: ConfigService
  ) {
    this.config = this.configService.getConfig();
  }

  ngOnInit() {
    this.loadCredentials();
    this.initializeRSA();
  }

  // Inicialização RSA
  private async initializeRSA() {
    if (!this.config.PRIVATE_KEY || !this.config.PUBLIC_KEY) {
      console.warn('Chaves RSA não configuradas');
      return;
    }

    try {
      await this.checkConsumerPublicKey();
    } catch (error) {
      console.error('Erro ao inicializar RSA:', error);
    }
  }

  private async checkConsumerPublicKey() {
    try {
      await this.apiService.hasValidPublicKey(this.config.CONSUMER_IDENTIFIER).toPromise();
    } catch (error) {
      console.log('Registrando chave pública...');
      await this.registerPublicKey();
    }
  }

  private async registerPublicKey() {
    if (!this.config.PUBLIC_KEY) {
      return;
    }

    const keyData = {
      consumerName: 'Frontend Credentials App',
      consumerIdentifier: this.config.CONSUMER_IDENTIFIER,
      publicKey: this.config.PUBLIC_KEY,
      description: 'Chave pública para aplicação frontend de gerenciamento de credenciais'
    };

    try {
      await this.apiService.createConsumerKey(keyData).toPromise();
      console.log('Chave pública registrada com sucesso');
    } catch (error) {
      console.error('Erro ao registrar chave pública:', error);
    }
  }

  // Carregamento de dados
  async loadCredentials() {
    this.isLoading = true;
    try {
      // Usar o endpoint que retorna credenciais com senhas criptografadas
      const credentialsResponse = await this.apiService.getAllCredentialsWithEncryptedPassword(
        this.config.CONSUMER_IDENTIFIER
      ).toPromise();
      
      // Converter CredentialsWithEncryptedPasswordResponse[] para CredentialWithUIState[]
      this.credentials = await Promise.all(
        (credentialsResponse || []).map(async (credentialResponse) => {
          // Descriptografar a senha criptografada
          let decryptedPassword = '••••••••';
          try {
            if (credentialResponse.encryptedPassword) {
              decryptedPassword = await this.decryptPassword(credentialResponse.encryptedPassword);
            }
          } catch (error) {
            console.warn('Não foi possível descriptografar a senha para a credencial:', credentialResponse.id);
          }

          // Descriptografar a senha do boleto se existir
          let decryptedPasswordOfInvoice: string | undefined = undefined;
          try {
            if (credentialResponse.encryptedPasswordOfInvoice) {
              decryptedPasswordOfInvoice = await this.decryptPassword(credentialResponse.encryptedPasswordOfInvoice);
            }
          } catch (error) {
            console.warn('Não foi possível descriptografar a senha do boleto para a credencial:', credentialResponse.id);
          }

          const credential: CredentialWithUIState = {
            id: credentialResponse.id,
            nameMall: credentialResponse.nameMall,
            urlPortal: credentialResponse.urlPortal,
            username: credentialResponse.username,
            cnpj: credentialResponse.cnpj,
            createdAt: credentialResponse.createdAt,
            updatedAt: credentialResponse.updatedAt,
            deletedAt: credentialResponse.deletedAt,
            active: credentialResponse.active,
            decryptedPassword: decryptedPassword,
            passwordOfInvoice: decryptedPasswordOfInvoice
          };
          
          return credential;
        })
      );
      
      this.filteredCredentials = [...this.credentials];
    } catch (error) {
      console.error('Erro ao carregar credenciais:', error);
      this.showAlertMessage('Erro ao carregar credenciais', 'error');
    } finally {
      this.isLoading = false;
    }
  }

  // Criptografia
  private async decryptPassword(encryptedPassword: string): Promise<string> {
    if (!this.config.PRIVATE_KEY || !encryptedPassword) {
      return '••••••••';
    }

    try {
      // Verificar se JSEncrypt está disponível globalmente
      if (typeof (window as any).JSEncrypt !== 'undefined') {
        const crypt = new (window as any).JSEncrypt();
        crypt.setPrivateKey(this.config.PRIVATE_KEY);
        const decrypted = crypt.decrypt(encryptedPassword);
        return decrypted || '••••••••';
      }
      return '••••••••';
    } catch (error) {
      console.error('Erro ao descriptografar senha:', error);
      return '••••••••';
    }
  }

  // Filtros e busca
  filterCredentials() {
    if (!this.searchTerm.trim()) {
      this.filteredCredentials = [...this.credentials];
      return;
    }

    const term = this.searchTerm.toLowerCase();
    this.filteredCredentials = this.credentials.filter(credential =>
      credential.nameMall.toLowerCase().includes(term) ||
      credential.urlPortal.toLowerCase().includes(term) ||
      credential.username.toLowerCase().includes(term)
    );
  }

  clearSearch() {
    this.searchTerm = '';
    this.filterCredentials();
  }

  // Controle de modais
  openCredentialModal(credential?: CredentialWithUIState) {
    if (credential) {
      this.editingCredential = credential;
      this.credentialFormData = {
        nameMall: credential.nameMall,
        urlPortal: credential.urlPortal,
        username: credential.username,
        password: credential.decryptedPassword || '',
        passwordOfInvoice: credential.passwordOfInvoice || '',
        cnpj: credential.cnpj || '',
        active: credential.active
      };
    } else {
      this.editingCredential = null;
      this.resetForm();
    }
    this.showCredentialModal = true;
  }

  closeCredentialModal() {
    this.showCredentialModal = false;
    this.editingCredential = null;
    this.resetForm();
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.deleteId = null;
  }

  closeDetailsModal() {
    this.showDetailsModal = false;
    this.selectedCredential = null;
    this.showDetailPassword = false;
  }

  closeModalOnBackdrop(event: MouseEvent) {
    if (event.target === event.currentTarget) {
      if (this.showCredentialModal) this.closeCredentialModal();
      if (this.showDeleteModal) this.closeDeleteModal();
      if (this.showDetailsModal) this.closeDetailsModal();
    }
  }

  // Formulário
  private resetForm() {
    this.credentialFormData = {
      nameMall: '',
      urlPortal: '',
      username: '',
      password: '',
      passwordOfInvoice: '',
      cnpj: '',
      active: true
    };
    this.showPassword = false;
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleDetailPassword() {
    this.showDetailPassword = !this.showDetailPassword;
  }

  applyCnpjMask(event: any) {
    let value = event.target.value.replace(/\D/g, '');
    value = value.replace(/^(\d{2})(\d)/, '$1.$2');
    value = value.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
    value = value.replace(/\.(\d{3})(\d)/, '.$1/$2');
    value = value.replace(/(\d{4})(\d)/, '$1-$2');
    event.target.value = value;
    this.credentialFormData.cnpj = value;
  }

  // CRUD Operations
  async saveCredential() {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    try {
      const credentialData: CredentialRequest = { ...this.credentialFormData };

      if (this.editingCredential) {
        // Atualizar credencial existente
        await this.apiService.updateCredential(
          this.editingCredential.id,
          credentialData
        ).toPromise();
        
        this.showAlertMessage('Credencial atualizada com sucesso!', 'success');
      } else {
        // Criar nova credencial
        await this.apiService.createCredential(credentialData).toPromise();
        this.showAlertMessage('Credencial criada com sucesso!', 'success');
      }

      // Recarregar a lista completa para manter os dados atualizados
      await this.loadCredentials();
      this.closeCredentialModal();
    } catch (error) {
      console.error('Erro ao salvar credencial:', error);
      this.showAlertMessage('Erro ao salvar credencial', 'error');
    } finally {
      this.isLoading = false;
    }
  }

  private validateForm(): boolean {
    if (!this.credentialFormData.nameMall.trim()) {
      this.showAlertMessage('Nome do shopping é obrigatório', 'warning');
      return false;
    }
    if (!this.credentialFormData.urlPortal.trim()) {
      this.showAlertMessage('URL é obrigatória', 'warning');
      return false;
    }
    if (!this.credentialFormData.username.trim()) {
      this.showAlertMessage('Usuário é obrigatório', 'warning');
      return false;
    }
    if (!this.credentialFormData.password.trim()) {
      this.showAlertMessage('Senha é obrigatória', 'warning');
      return false;
    }
    if (!this.isValidUrl(this.credentialFormData.urlPortal)) {
      this.showAlertMessage('URL inválida', 'warning');
      return false;
    }
    return true;
  }

  private isValidUrl(string: string): boolean {
    try {
      new URL(string);
      return true;
    } catch {
      return false;
    }
  }

  editCredential(credential: CredentialWithUIState) {
    this.openCredentialModal(credential);
  }

  confirmDeleteCredential(id: string) {
    this.deleteId = id;
    this.showDeleteModal = true;
  }

  async deleteCredential() {
    if (!this.deleteId) return;

    this.isLoading = true;
    try {
      await this.apiService.deleteCredential(this.deleteId).toPromise();
      
      this.credentials = this.credentials.filter(c => c.id !== this.deleteId);
      this.filterCredentials();
      this.showAlertMessage('Credencial excluída com sucesso!', 'success');
      this.closeDeleteModal();
    } catch (error) {
      console.error('Erro ao excluir credencial:', error);
      this.showAlertMessage('Erro ao excluir credencial', 'error');
    } finally {
      this.isLoading = false;
    }
  }

  // Detalhes e visualização
  async showDetails(credential: CredentialWithUIState) {
    this.selectedCredential = credential;
    this.showDetailsModal = true;
  }

  editFromDetails() {
    if (this.selectedCredential) {
      this.closeDetailsModal();
      this.openCredentialModal(this.selectedCredential);
    }
  }

  // Funcionalidades da tabela
  async togglePasswordInTable(credential: CredentialWithUIState) {
    credential.showPassword = !credential.showPassword;
  }

  // Funcionalidades de cópia
  async copyToClipboard(text: string, type: string = 'texto') {
    try {
      await navigator.clipboard.writeText(text);
      this.showAlertMessage(`${type} copiado para a área de transferência!`, 'success');
    } catch (error) {
      console.error('Erro ao copiar:', error);
      this.showAlertMessage(`Erro ao copiar ${type}`, 'error');
    }
  }

  async copyPasswordToClipboard(credentialId: string) {
    try {
      // Encontrar a credencial na lista local
      const credential = this.credentials.find(c => c.id === credentialId);
      if (credential?.decryptedPassword) {
        await this.copyToClipboard(credential.decryptedPassword, 'senha');
      } else {
        this.showAlertMessage('Senha não disponível', 'warning');
      }
    } catch (error) {
      console.error('Erro ao copiar senha:', error);
      this.showAlertMessage('Erro ao copiar senha', 'error');
    }
  }

  // Utilitários
  extractDomain(url: string): string {
    try {
      const domain = new URL(url).hostname;
      return domain.replace('www.', '');
    } catch {
      return url;
    }
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString('pt-BR');
  }

  trackByCredentialId(index: number, credential: CredentialWithUIState): string {
    return credential.id;
  }

  // Sistema de alertas
  showAlertMessage(message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info') {
    this.alertMessage = message;
    this.alertType = type;
    this.showAlert = true;
    
    // Auto-hide após 5 segundos
    setTimeout(() => {
      this.closeAlert();
    }, 5000);
  }

  closeAlert() {
    this.showAlert = false;
    this.alertMessage = '';
  }

  getAlertIcon(): string {
    switch (this.alertType) {
      case 'success': return 'bi bi-check-circle-fill text-success';
      case 'error': return 'bi bi-exclamation-triangle-fill text-danger';
      case 'warning': return 'bi bi-exclamation-triangle-fill text-warning';
      default: return 'bi bi-info-circle-fill text-info';
    }
  }

  getAlertTitle(): string {
    switch (this.alertType) {
      case 'success': return 'Sucesso';
      case 'error': return 'Erro';
      case 'warning': return 'Atenção';
      default: return 'Informação';
    }
  }
}
