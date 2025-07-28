/**
 * Interfaces relacionadas aos Consumer Keys (Chaves Públicas dos Consumidores)
 */

export interface ConsumerPublicKeyRequest {
  consumerName: string;
  consumerIdentifier: string;
  publicKey: string;
  expiresAt?: string;
  description?: string;
}

export interface ConsumerPublicKeyResponse {
  id: string;
  consumerName: string;
  consumerIdentifier: string;
  keyAlgorithm: string;
  keySize: number;
  createdAt: string;
  updatedAt: string;
  expiresAt?: string;
  active: boolean;
  description?: string;
  isValid: boolean;
}
