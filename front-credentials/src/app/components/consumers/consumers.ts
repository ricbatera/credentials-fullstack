import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { ConsumerPublicKeyResponse, ConsumerPublicKeyRequest } from '../../interfaces';

@Component({
  selector: 'app-consumers',
  imports: [CommonModule, FormsModule],
  templateUrl: './consumers.html',
  styleUrl: './consumers.css'
})
export class Consumers implements OnInit {
  protected consumerKeys = signal<ConsumerPublicKeyResponse[]>([]);
  protected loading = signal(false);
  protected showAddForm = signal(false);
  protected error = signal<string | null>(null);
  protected showCredentialsModal = signal(false);
  protected selectedConsumerId = signal<string | null>(null);

  // Formulário para novo consumer
  protected newConsumer = {
    consumerName: '',
    consumerIdentifier: '',
    publicKey: '',
    description: '',
    expiresAt: ''
  };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadConsumerKeys();
  }

  private loadConsumerKeys() {
    this.loading.set(true);
    this.error.set(null);
    
    this.apiService.getAllConsumerKeys().subscribe({
      next: (keys) => {
        this.consumerKeys.set(keys);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Erro ao carregar chaves:', error);
        this.error.set('Erro ao carregar os consumers. Verifique se o backend está rodando.');
        this.loading.set(false);
      }
    });
  }

  protected toggleAddForm() {
    this.showAddForm.set(!this.showAddForm());
    if (!this.showAddForm()) {
      this.resetForm();
    }
  }

  protected generateKeyPair() {
    this.apiService.generateKeyPairExample().subscribe({
      next: (keyPair) => {
        // Extrair apenas a chave pública do resultado
        const publicKeyMatch = keyPair.match(/-----BEGIN PUBLIC KEY-----[\s\S]*?-----END PUBLIC KEY-----/);
        if (publicKeyMatch) {
          this.newConsumer.publicKey = publicKeyMatch[0];
        }
      },
      error: (error) => {
        console.error('Erro ao gerar par de chaves:', error);
        this.error.set('Erro ao gerar par de chaves.');
      }
    });
  }

  protected addConsumer() {
    if (this.isFormValid()) {
      this.loading.set(true);
      
      const request: ConsumerPublicKeyRequest = {
        consumerName: this.newConsumer.consumerName,
        consumerIdentifier: this.newConsumer.consumerIdentifier,
        publicKey: this.newConsumer.publicKey,
        description: this.newConsumer.description
      };

      if (this.newConsumer.expiresAt) {
        request.expiresAt = new Date(this.newConsumer.expiresAt).toISOString();
      }

      this.apiService.createConsumerKey(request).subscribe({
        next: (newKey) => {
          this.consumerKeys.update(keys => [...keys, newKey]);
          this.resetForm();
          this.showAddForm.set(false);
          this.loading.set(false);
          this.error.set(null);
        },
        error: (error) => {
          console.error('Erro ao criar consumer:', error);
          this.error.set('Erro ao criar consumer. Verifique os dados e tente novamente.');
          this.loading.set(false);
        }
      });
    }
  }

  protected deleteConsumer(id: string) {
    if (confirm('Tem certeza que deseja remover este consumer?')) {
      this.loading.set(true);
      
      this.apiService.deleteConsumerKey(id).subscribe({
        next: () => {
          this.consumerKeys.update(keys => keys.filter(key => key.id !== id));
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Erro ao remover consumer:', error);
          this.error.set('Erro ao remover consumer.');
          this.loading.set(false);
        }
      });
    }
  }

  private isFormValid(): boolean {
    return !!(this.newConsumer.consumerName && 
              this.newConsumer.consumerIdentifier && 
              this.newConsumer.publicKey);
  }

  private resetForm() {
    this.newConsumer = {
      consumerName: '',
      consumerIdentifier: '',
      publicKey: '',
      description: '',
      expiresAt: ''
    };
  }

  protected formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('pt-BR');
  }

  protected openCredentialsModal(consumerId: string) {
    this.selectedConsumerId.set(consumerId);
    this.showCredentialsModal.set(true);
  }

  protected closeCredentialsModal() {
    this.showCredentialsModal.set(false);
    this.selectedConsumerId.set(null);
  }
}
