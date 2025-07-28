# Interfaces Centralizadas

Este diretório contém todas as interfaces TypeScript utilizadas no projeto, organizadas de forma centralizada para facilitar a reutilização e manutenção.

## Estrutura

```
src/app/interfaces/
├── index.ts                      # Arquivo barrel para exportações
├── consumer-key.interface.ts     # Interfaces relacionadas às chaves dos consumidores
└── credential.interface.ts       # Interfaces relacionadas às credenciais
```

## Como usar

### Importação simplificada
```typescript
import { 
  CredentialRequest, 
  CredentialResponse, 
  ConsumerPublicKeyRequest 
} from '../interfaces';
```

### Nos componentes
```typescript
import { Component } from '@angular/core';
import { CredentialResponse, CredentialRequest } from '../../interfaces';

export class MyComponent {
  credentials: CredentialResponse[] = [];
  
  createCredential(data: CredentialRequest) {
    // implementação
  }
}
```

### Nos serviços
```typescript
import { Injectable } from '@angular/core';
import { CredentialRequest, CredentialResponse } from '../interfaces';

@Injectable()
export class MyService {
  processCredential(credential: CredentialResponse): void {
    // implementação
  }
}
```

## Interfaces Disponíveis

### Consumer Keys
- `ConsumerPublicKeyRequest` - Para requisições de criação/atualização de chaves
- `ConsumerPublicKeyResponse` - Para respostas da API de chaves

### Credentials
- `CredentialRequest` - Para requisições de criação/atualização de credenciais
- `CredentialResponse` - Para respostas da API de credenciais
- `PasswordVerificationRequest` - Para verificação de senhas
- `CredentialsWithEncryptedPasswordResponse` - Para credenciais com senhas criptografadas
- `BasicCredentialsResponse` - Para credenciais básicas consumidas por robôs

## Benefícios

1. **Reutilização**: Evita duplicação de código
2. **Manutenção**: Mudanças centralizadas em um só lugar
3. **Consistência**: Garante tipagem uniforme em todo o projeto
4. **Organização**: Separação clara de responsabilidades
5. **Facilidade de importação**: Arquivo barrel para importações simplificadas
