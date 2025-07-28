/**
 * Interfaces relacionadas às Credenciais
 */

export interface CredentialRequest {
  nameMall: string;
  urlPortal: string;
  username: string;
  password: string;
  passwordOfInvoice?: string;
  cnpj?: string;
  active: boolean;
}

export interface CredentialResponse {
  id: string;
  nameMall: string;
  urlPortal: string;
  username: string;
  passwordOfInvoice?: string;
  cnpj?: string;
  createdAt: string;
  updatedAt: string;
  deletedAt?: string;
  active: boolean;
}

export interface PasswordResponse {
  password: string;
}

export interface PasswordVerificationRequest {
  password: string;
}

export interface CredentialsWithEncryptedPasswordResponse {
  id: string;
  nameMall: string;
  cnpj?: string;
  urlPortal: string;
  username: string;
  encryptedPassword: string;
  encryptedPasswordOfInvoice?: string;
  consumerIdentifier: string;
  encryptionAlgorithm: string;
  createdAt: string;
  updatedAt: string;
  deletedAt?: string;
  active: boolean;
}

export interface BasicCredentialsResponse {
  urlPortal: string;
  username: string;
  password: string;
  passwordOfInvoice?: string;
  nameMall: string;
}
