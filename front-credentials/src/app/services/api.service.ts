import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from './config.service';
import {
  ConsumerPublicKeyRequest,
  ConsumerPublicKeyResponse,
  CredentialRequest,
  CredentialResponse,
  PasswordVerificationRequest,
  CredentialsWithEncryptedPasswordResponse,
  BasicCredentialsResponse
} from '../interfaces';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  //private readonly baseUrl = 'http://localhost:8084/api';
  private readonly baseUrlConsumerKey: string;
  private readonly baseUrlCredentials:string;
  
  private readonly httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor(private http: HttpClient, private configService: ConfigService) {
    this.baseUrlConsumerKey = this.configService.getApiBaseUrlConsumerKey();
    this.baseUrlCredentials = this.configService.getApiBaseUrlCredentials();
  }

  // Consumer Keys endpoints
  getAllConsumerKeys(): Observable<ConsumerPublicKeyResponse[]> {
    return this.http.get<ConsumerPublicKeyResponse[]>(`${this.baseUrlConsumerKey}`);
  }

  createConsumerKey(data: ConsumerPublicKeyRequest): Observable<ConsumerPublicKeyResponse> {
    return this.http.post<ConsumerPublicKeyResponse>(
      `${this.baseUrlConsumerKey}`,
      data,
      this.httpOptions
    );
  }

  getConsumerKeyById(id: string): Observable<ConsumerPublicKeyResponse> {
    return this.http.get<ConsumerPublicKeyResponse>(`${this.baseUrlConsumerKey}/${id}`);
  }

  updateConsumerKey(id: string, data: ConsumerPublicKeyRequest): Observable<ConsumerPublicKeyResponse> {
    return this.http.put<ConsumerPublicKeyResponse>(
      `${this.baseUrlConsumerKey}/${id}`,
      data,
      this.httpOptions
    );
  }

  deleteConsumerKey(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrlConsumerKey}/${id}`);
  }

  getConsumerKeyByIdentifier(consumerIdentifier: string): Observable<ConsumerPublicKeyResponse> {
    return this.http.get<ConsumerPublicKeyResponse>(
      `${this.baseUrlConsumerKey}/consumer/${consumerIdentifier}`
    );
  }

  getValidConsumerKeys(): Observable<ConsumerPublicKeyResponse[]> {
    return this.http.get<ConsumerPublicKeyResponse[]>(`${this.baseUrlConsumerKey}/valid`);
  }

  hasValidPublicKey(consumerIdentifier: string): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.baseUrlConsumerKey}/consumer/${consumerIdentifier}/valid`
    );
  }

  generateKeyPairExample(): Observable<string> {
    return this.http.get(`${this.baseUrlConsumerKey}/generate-example`, {
      responseType: 'text'
    });
  }

  deleteConsumerKeyByIdentifier(consumerIdentifier: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrlConsumerKey}/consumer/${consumerIdentifier}`);
  }

  getBasicCredentials(consumerIdentifier: string): Observable<BasicCredentialsResponse[]> {
    return this.http.get<BasicCredentialsResponse[]>(`${this.baseUrlConsumerKey}/credentials`, {
      headers: {
        ...this.httpOptions.headers,
        'X-Consumer-Identifier': consumerIdentifier
      }
    });
  }

  // Credentials endpoints
  getAllCredentials(): Observable<CredentialResponse[]> {
    return this.http.get<CredentialResponse[]>(`${this.baseUrlCredentials}`);
  }

  createCredential(data: CredentialRequest): Observable<CredentialResponse> {
    return this.http.post<CredentialResponse>(
      `${this.baseUrlCredentials}`,
      data,
      this.httpOptions
    );
  }

  getCredentialById(id: string): Observable<CredentialResponse> {
    return this.http.get<CredentialResponse>(`${this.baseUrlCredentials}/${id}`);
  }

  updateCredential(id: string, data: CredentialRequest): Observable<CredentialResponse> {
    return this.http.put<CredentialResponse>(
      `${this.baseUrlCredentials}/${id}`,
      data,
      this.httpOptions
    );
  }

  deleteCredential(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrlCredentials}/${id}`);
  }

  verifyPassword(id: string, passwordData: PasswordVerificationRequest): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrlCredentials}/${id}/verify-password`,
      passwordData,
      this.httpOptions
    );
  }

  encryptPasswordForConsumer(consumerIdentifier: string, passwordData: PasswordVerificationRequest): Observable<string> {
    return this.http.post(
      `${this.baseUrlCredentials}/encrypt-password/${consumerIdentifier}`,
      passwordData,
      {
        headers: this.httpOptions.headers,
        responseType: 'text'
      }
    );
  }

  getCredentialWithEncryptedPassword(id: string, consumerIdentifier: string): Observable<CredentialsWithEncryptedPasswordResponse> {
    return this.http.get<CredentialsWithEncryptedPasswordResponse>(
      `${this.baseUrlCredentials}/${id}/encrypted/${consumerIdentifier}`
    );
  }

  getCredentialsByShoppingName(nameMall: string): Observable<CredentialResponse[]> {
    return this.http.get<CredentialResponse[]>(
      `${this.baseUrlCredentials}/search?nameMall=${encodeURIComponent(nameMall)}`
    );
  }

  getAllCredentialsWithEncryptedPassword(consumerIdentifier: string): Observable<CredentialsWithEncryptedPasswordResponse[]> {
    return this.http.get<CredentialsWithEncryptedPasswordResponse[]>(
      `${this.baseUrlCredentials}/encrypted/${consumerIdentifier}`
    );
  }

  getCredentialByCnpj(cnpj: string): Observable<CredentialResponse> {
    return this.http.get<CredentialResponse>(`${this.baseUrlCredentials}/cnpj/${cnpj}`);
  }
}
