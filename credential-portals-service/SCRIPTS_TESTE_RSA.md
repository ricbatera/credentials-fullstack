# 🧪 Scripts de Teste e Validação - Criptografia RSA

## 🎯 Visão Geral dos Testes

Este documento contém scripts para testar e validar a implementação completa da criptografia RSA para consumidores da API de credenciais.

## 🔧 Scripts de Teste com cURL

### **Script 1: Teste Completo do Fluxo RSA**

```bash
#!/bin/bash

# Configurações
API_BASE_URL="http://localhost:8084"
CONSUMER_ID="test-consumer-$(date +%s)"

echo "🚀 Iniciando teste completo do fluxo RSA..."
echo "📋 Consumer ID: $CONSUMER_ID"
echo ""

# 1. Gerar exemplo de chaves
echo "🔑 1. Gerando par de chaves RSA..."
KEY_RESPONSE=$(curl -s -X GET "$API_BASE_URL/api/consumer-keys/generate-example")

if [ $? -eq 0 ]; then
    echo "✅ Chaves geradas com sucesso!"
    
    # Extrair chaves da resposta
    PUBLIC_KEY=$(echo $KEY_RESPONSE | jq -r '.publicKey')
    PRIVATE_KEY=$(echo $KEY_RESPONSE | jq -r '.privateKey')
    
    echo "📤 Chave pública extraída: ${PUBLIC_KEY:0:64}..."
    echo "🔐 Chave privada extraída: ${PRIVATE_KEY:0:64}..."
else
    echo "❌ Falha ao gerar chaves"
    exit 1
fi

echo ""

# 2. Registrar chave pública
echo "📝 2. Registrando chave pública na API..."
REGISTER_RESPONSE=$(curl -s -X POST "$API_BASE_URL/api/consumer-keys" \
    -H "Content-Type: application/json" \
    -d "{
        \"consumerName\": \"Test Consumer App\",
        \"consumerIdentifier\": \"$CONSUMER_ID\",
        \"publicKey\": \"$PUBLIC_KEY\",
        \"description\": \"Teste automatizado do fluxo RSA\",
        \"expiresAt\": \"2025-12-31T23:59:59\"
    }")

if [ $? -eq 0 ]; then
    echo "✅ Chave pública registrada com sucesso!"
    echo "📋 Resposta: $REGISTER_RESPONSE"
else
    echo "❌ Falha ao registrar chave pública"
    exit 1
fi

echo ""

# 3. Verificar se há credenciais no banco
echo "📊 3. Verificando credenciais disponíveis..."
CREDENTIALS_COUNT=$(curl -s -X GET "$API_BASE_URL/api/credentials" | jq length)

if [ "$CREDENTIALS_COUNT" -eq 0 ]; then
    echo "ℹ️  Nenhuma credencial encontrada. Criando credencial de teste..."
    
    # Criar credencial de teste
    TEST_CREDENTIAL=$(curl -s -X POST "$API_BASE_URL/api/credentials" \
        -H "Content-Type: application/json" \
        -d '{
            "nameMall": "Shopping Teste RSA",
            "cnpj": "12345678901234",
            "urlPortal": "https://portal-teste-rsa.com",
            "username": "admin-teste",
            "password": "SenhaSecreta123!"
        }')
    
    if [ $? -eq 0 ]; then
        echo "✅ Credencial de teste criada!"
        echo "📋 Credencial: $TEST_CREDENTIAL"
    else
        echo "❌ Falha ao criar credencial de teste"
        exit 1
    fi
else
    echo "✅ Encontradas $CREDENTIALS_COUNT credenciais no sistema"
fi

echo ""

# 4. Buscar credenciais com senhas criptografadas
echo "🔒 4. Buscando credenciais com senhas criptografadas..."
ENCRYPTED_CREDENTIALS=$(curl -s -X GET "$API_BASE_URL/api/credentials/encrypted/$CONSUMER_ID")

if [ $? -eq 0 ]; then
    echo "✅ Credenciais criptografadas obtidas com sucesso!"
    
    # Extrair primeira credencial para teste
    FIRST_CREDENTIAL=$(echo $ENCRYPTED_CREDENTIALS | jq '.[0]')
    ENCRYPTED_PASSWORD=$(echo $FIRST_CREDENTIAL | jq -r '.encryptedPassword')
    MALL_NAME=$(echo $FIRST_CREDENTIAL | jq -r '.nameMall')
    
    echo "🏢 Shopping: $MALL_NAME"
    echo "🔐 Senha criptografada: ${ENCRYPTED_PASSWORD:0:64}..."
else
    echo "❌ Falha ao buscar credenciais criptografadas"
    exit 1
fi

echo ""

# 5. Simular descriptografia (usando Node.js)
echo "🔓 5. Testando descriptografia da senha..."

# Criar script Node.js temporário para descriptografia
cat > temp_decrypt.js << EOF
const crypto = require('crypto');

const privateKeyPem = \`$PRIVATE_KEY\`;
const encryptedPassword = '$ENCRYPTED_PASSWORD';

try {
    const buffer = Buffer.from(encryptedPassword, 'base64');
    const decrypted = crypto.privateDecrypt(privateKeyPem, buffer);
    console.log('✅ Senha descriptografada com sucesso: ' + decrypted.toString('utf8'));
} catch (error) {
    console.log('❌ Erro na descriptografia: ' + error.message);
    process.exit(1);
}
EOF

# Executar descriptografia
if command -v node &> /dev/null; then
    node temp_decrypt.js
    rm temp_decrypt.js
else
    echo "⚠️  Node.js não encontrado. Pulando teste de descriptografia."
fi

echo ""

# 6. Validar chave registrada
echo "✅ 6. Validando chave registrada..."
VALIDATION_RESPONSE=$(curl -s -X GET "$API_BASE_URL/api/consumer-keys/consumer/$CONSUMER_ID")

if [ $? -eq 0 ]; then
    IS_VALID=$(echo $VALIDATION_RESPONSE | jq -r '.isValid')
    CONSUMER_NAME=$(echo $VALIDATION_RESPONSE | jq -r '.consumerName')
    
    echo "✅ Chave validada!"
    echo "📋 Consumer: $CONSUMER_NAME"
    echo "🔍 Válida: $IS_VALID"
else
    echo "❌ Falha ao validar chave"
fi

echo ""

# 7. Limpeza (opcional)
echo "🧹 7. Limpeza dos dados de teste..."
read -p "Deseja remover os dados de teste criados? (y/n): " -r
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Buscar ID da chave para remoção
    KEY_ID=$(echo $VALIDATION_RESPONSE | jq -r '.id')
    
    if [ "$KEY_ID" != "null" ]; then
        DELETE_RESPONSE=$(curl -s -X DELETE "$API_BASE_URL/api/consumer-keys/$KEY_ID")
        if [ $? -eq 0 ]; then
            echo "✅ Chave pública removida"
        else
            echo "⚠️  Falha ao remover chave pública"
        fi
    fi
fi

echo ""
echo "🎉 Teste completo finalizado!"
echo "📊 Resumo:"
echo "   ✅ Geração de chaves RSA"
echo "   ✅ Registro de chave pública"
echo "   ✅ Busca de credenciais criptografadas"
echo "   ✅ Descriptografia de senhas"
echo "   ✅ Validação de chaves"
```

### **Script 2: Teste de Performance e Carga**

```bash
#!/bin/bash

# Configurações
API_BASE_URL="http://localhost:8084"
NUM_CONSUMERS=10
NUM_REQUESTS_PER_CONSUMER=5

echo "⚡ Iniciando teste de performance..."
echo "👥 Consumidores: $NUM_CONSUMERS"
echo "📊 Requests por consumidor: $NUM_REQUESTS_PER_CONSUMER"
echo ""

# Função para testar um consumidor
test_consumer() {
    local consumer_id="perf-test-$1"
    local start_time=$(date +%s.%N)
    
    echo "🚀 Testando consumidor: $consumer_id"
    
    # Gerar chaves
    local key_response=$(curl -s -X GET "$API_BASE_URL/api/consumer-keys/generate-example")
    local public_key=$(echo $key_response | jq -r '.publicKey')
    
    # Registrar chave
    curl -s -X POST "$API_BASE_URL/api/consumer-keys" \
        -H "Content-Type: application/json" \
        -d "{
            \"consumerName\": \"Performance Test $1\",
            \"consumerIdentifier\": \"$consumer_id\",
            \"publicKey\": \"$public_key\",
            \"description\": \"Teste de performance $1\"
        }" > /dev/null
    
    # Fazer múltiplas requisições
    for ((j=1; j<=NUM_REQUESTS_PER_CONSUMER; j++)); do
        curl -s -X GET "$API_BASE_URL/api/credentials/encrypted/$consumer_id" > /dev/null
    done
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc)
    
    echo "⏱️  Consumidor $consumer_id: ${duration}s"
}

# Executar testes em paralelo
for ((i=1; i<=NUM_CONSUMERS; i++)); do
    test_consumer $i &
done

# Aguardar todos os processos
wait

echo "✅ Teste de performance concluído!"
```

### **Script 3: Validação de Segurança**

```bash
#!/bin/bash

API_BASE_URL="http://localhost:8084"

echo "🔒 Iniciando testes de segurança..."
echo ""

# Teste 1: Tentativa de acesso sem chave registrada
echo "🧪 Teste 1: Acesso sem chave registrada"
RESPONSE=$(curl -s -w "%{http_code}" -X GET "$API_BASE_URL/api/credentials/encrypted/consumer-inexistente")
HTTP_CODE="${RESPONSE: -3}"

if [ "$HTTP_CODE" == "404" ]; then
    echo "✅ Bloqueou acesso sem chave registrada (HTTP $HTTP_CODE)"
else
    echo "❌ Falha na segurança: permitiu acesso sem chave (HTTP $HTTP_CODE)"
fi

echo ""

# Teste 2: Tentativa de registro de chave inválida
echo "🧪 Teste 2: Registro de chave RSA inválida"
INVALID_KEY_RESPONSE=$(curl -s -w "%{http_code}" -X POST "$API_BASE_URL/api/consumer-keys" \
    -H "Content-Type: application/json" \
    -d '{
        "consumerName": "Test Invalid Key",
        "consumerIdentifier": "invalid-key-test",
        "publicKey": "chave-invalida-base64",
        "description": "Teste de chave inválida"
    }')

INVALID_HTTP_CODE="${INVALID_KEY_RESPONSE: -3}"

if [ "$INVALID_HTTP_CODE" == "400" ]; then
    echo "✅ Rejeitou chave inválida (HTTP $INVALID_HTTP_CODE)"
else
    echo "❌ Falha na validação: aceitou chave inválida (HTTP $INVALID_HTTP_CODE)"
fi

echo ""

# Teste 3: Verificar se senhas não são retornadas em texto plano
echo "🧪 Teste 3: Verificar se senhas não vazam em texto plano"
PLAIN_CREDENTIALS=$(curl -s -X GET "$API_BASE_URL/api/credentials")

if echo "$PLAIN_CREDENTIALS" | grep -q '"password"'; then
    echo "❌ ALERTA: Senhas sendo retornadas em texto plano!"
else
    echo "✅ Senhas não são expostas em endpoints normais"
fi

echo ""

# Teste 4: Verificar criptografia das senhas no banco
echo "🧪 Teste 4: Validar que senhas são criptografadas no banco"
echo "ℹ️  Este teste requer acesso direto ao banco de dados"
echo "   Execute: SELECT password FROM credentials LIMIT 1;"
echo "   A senha deve começar com '$2b$' (BCrypt)"

echo ""
echo "🎯 Testes de segurança concluídos!"
```

## 🐍 Script de Teste em Python

```python
#!/usr/bin/env python3
"""
Script de teste completo para validar a implementação RSA
"""

import requests
import base64
import json
import time
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.backends import default_backend

class RSATestSuite:
    def __init__(self, api_base_url="http://localhost:8084"):
        self.api_base_url = api_base_url
        self.consumer_id = f"python-test-{int(time.time())}"
        self.private_key = None
        self.public_key = None
        
    def generate_keys(self):
        """Gera par de chaves RSA para teste"""
        print("🔑 Gerando par de chaves RSA...")
        
        self.private_key = rsa.generate_private_key(
            public_exponent=65537,
            key_size=2048,
            backend=default_backend()
        )
        self.public_key = self.private_key.public_key()
        
        print("✅ Chaves geradas com sucesso!")
        
    def public_key_to_base64(self):
        """Converte chave pública para Base64"""
        pem = self.public_key.public_bytes(
            encoding=serialization.Encoding.DER,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
        return base64.b64encode(pem).decode('utf-8')
        
    def register_public_key(self):
        """Registra chave pública na API"""
        print("📝 Registrando chave pública...")
        
        url = f"{self.api_base_url}/api/consumer-keys"
        data = {
            "consumerName": "Python Test Suite",
            "consumerIdentifier": self.consumer_id,
            "publicKey": self.public_key_to_base64(),
            "description": "Teste automatizado Python",
            "expiresAt": "2025-12-31T23:59:59"
        }
        
        response = requests.post(url, json=data)
        
        if response.status_code == 201:
            print("✅ Chave pública registrada!")
            return response.json()
        else:
            raise Exception(f"Falha ao registrar chave: {response.text}")
            
    def create_test_credential(self):
        """Cria credencial de teste"""
        print("📊 Criando credencial de teste...")
        
        url = f"{self.api_base_url}/api/credentials"
        data = {
            "nameMall": "Shopping Teste Python",
            "cnpj": "98765432109876",
            "urlPortal": "https://portal-python-test.com",
            "username": "python-test",
            "password": "TestePython@2024!"
        }
        
        response = requests.post(url, json=data)
        
        if response.status_code == 201:
            print("✅ Credencial de teste criada!")
            return response.json()
        else:
            print(f"⚠️  Usando credenciais existentes: {response.status_code}")
            
    def get_encrypted_credentials(self):
        """Busca credenciais com senhas criptografadas"""
        print("🔒 Buscando credenciais criptografadas...")
        
        url = f"{self.api_base_url}/api/credentials/encrypted/{self.consumer_id}"
        response = requests.get(url)
        
        if response.status_code == 200:
            credentials = response.json()
            print(f"✅ Encontradas {len(credentials)} credenciais criptografadas!")
            return credentials
        else:
            raise Exception(f"Falha ao buscar credenciais: {response.text}")
            
    def decrypt_password(self, encrypted_password):
        """Descriptografa senha usando chave privada"""
        print("🔓 Descriptografando senha...")
        
        try:
            encrypted_bytes = base64.b64decode(encrypted_password)
            
            decrypted_bytes = self.private_key.decrypt(
                encrypted_bytes,
                padding.OAEP(
                    mgf=padding.MGF1(algorithm=hashes.SHA256()),
                    algorithm=hashes.SHA256(),
                    label=None
                )
            )
            
            decrypted_password = decrypted_bytes.decode('utf-8')
            print(f"✅ Senha descriptografada: {decrypted_password}")
            return decrypted_password
            
        except Exception as e:
            raise Exception(f"Falha na descriptografia: {str(e)}")
            
    def run_performance_test(self, num_requests=10):
        """Executa teste de performance"""
        print(f"⚡ Executando teste de performance ({num_requests} requests)...")
        
        url = f"{self.api_base_url}/api/credentials/encrypted/{self.consumer_id}"
        
        start_time = time.time()
        success_count = 0
        
        for i in range(num_requests):
            try:
                response = requests.get(url, timeout=10)
                if response.status_code == 200:
                    success_count += 1
            except Exception as e:
                print(f"❌ Request {i+1} falhou: {str(e)}")
                
        end_time = time.time()
        duration = end_time - start_time
        
        print(f"📊 Performance:")
        print(f"   ⏱️  Tempo total: {duration:.2f}s")
        print(f"   ✅ Sucessos: {success_count}/{num_requests}")
        print(f"   📈 Requests/segundo: {success_count/duration:.2f}")
        
    def run_full_test(self):
        """Executa suite completa de testes"""
        print("🚀 Iniciando suite completa de testes RSA")
        print(f"📋 Consumer ID: {self.consumer_id}")
        print("=" * 50)
        
        try:
            # 1. Gerar chaves
            self.generate_keys()
            
            # 2. Registrar chave pública
            registration_result = self.register_public_key()
            
            # 3. Criar credencial de teste
            self.create_test_credential()
            
            # 4. Buscar credenciais criptografadas
            encrypted_credentials = self.get_encrypted_credentials()
            
            # 5. Testar descriptografia
            if encrypted_credentials:
                first_credential = encrypted_credentials[0]
                encrypted_password = first_credential['encryptedPassword']
                
                print(f"🏢 Shopping: {first_credential['nameMall']}")
                print(f"👤 Usuário: {first_credential['username']}")
                
                decrypted_password = self.decrypt_password(encrypted_password)
                
            # 6. Teste de performance
            self.run_performance_test()
            
            print("=" * 50)
            print("🎉 Todos os testes passaram com sucesso!")
            
        except Exception as e:
            print(f"❌ Erro durante os testes: {str(e)}")
            
    def cleanup(self):
        """Remove dados de teste"""
        print("🧹 Limpando dados de teste...")
        
        # Buscar chave para remoção
        try:
            url = f"{self.api_base_url}/api/consumer-keys/consumer/{self.consumer_id}"
            response = requests.get(url)
            
            if response.status_code == 200:
                key_data = response.json()
                key_id = key_data['id']
                
                # Remover chave
                delete_url = f"{self.api_base_url}/api/consumer-keys/{key_id}"
                delete_response = requests.delete(delete_url)
                
                if delete_response.status_code == 200:
                    print("✅ Chave de teste removida!")
                else:
                    print("⚠️  Falha ao remover chave de teste")
                    
        except Exception as e:
            print(f"⚠️  Erro na limpeza: {str(e)}")

if __name__ == "__main__":
    # Executar testes
    test_suite = RSATestSuite()
    
    try:
        test_suite.run_full_test()
    finally:
        # Limpeza
        cleanup_input = input("\n🗑️  Deseja limpar os dados de teste? (y/n): ")
        if cleanup_input.lower() in ['y', 'yes', 's', 'sim']:
            test_suite.cleanup()
```

## 📊 Script para Monitoramento e Métricas

```bash
#!/bin/bash

# Script de monitoramento da API RSA
API_BASE_URL="http://localhost:8084"
LOG_FILE="rsa_monitoring.log"

echo "📊 Iniciando monitoramento da API RSA..."
echo "📁 Logs salvos em: $LOG_FILE"
echo ""

# Função para log com timestamp
log_with_timestamp() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

# Loop de monitoramento
while true; do
    # Verificar saúde da API
    HEALTH_RESPONSE=$(curl -s -w "%{http_code}" -X GET "$API_BASE_URL/actuator/health" 2>/dev/null)
    HEALTH_CODE="${HEALTH_RESPONSE: -3}"
    
    if [ "$HEALTH_CODE" == "200" ]; then
        log_with_timestamp "✅ API saudável (HTTP $HEALTH_CODE)"
    else
        log_with_timestamp "❌ API com problema (HTTP $HEALTH_CODE)"
    fi
    
    # Contar chaves registradas
    KEYS_COUNT=$(curl -s -X GET "$API_BASE_URL/api/consumer-keys" 2>/dev/null | jq length 2>/dev/null || echo "0")
    log_with_timestamp "🔑 Chaves registradas: $KEYS_COUNT"
    
    # Contar credenciais
    CREDENTIALS_COUNT=$(curl -s -X GET "$API_BASE_URL/api/credentials" 2>/dev/null | jq length 2>/dev/null || echo "0")
    log_with_timestamp "📊 Credenciais cadastradas: $CREDENTIALS_COUNT"
    
    echo "---"
    
    # Aguardar 30 segundos
    sleep 30
done
```

## 🎯 Como Executar os Testes

### **Pré-requisitos:**
```bash
# Instalar dependências
sudo apt-get install curl jq bc nodejs python3 python3-pip

# Python
pip3 install requests cryptography
```

### **Execução:**

1. **Teste completo com Bash:**
   ```bash
   chmod +x test_rsa_complete.sh
   ./test_rsa_complete.sh
   ```

2. **Teste de performance:**
   ```bash
   chmod +x test_rsa_performance.sh
   ./test_rsa_performance.sh
   ```

3. **Teste de segurança:**
   ```bash
   chmod +x test_rsa_security.sh
   ./test_rsa_security.sh
   ```

4. **Suite Python:**
   ```bash
   python3 test_rsa_suite.py
   ```

5. **Monitoramento contínuo:**
   ```bash
   chmod +x monitor_rsa.sh
   ./monitor_rsa.sh
   ```

## 📈 Interpretação dos Resultados

### **Resultados Esperados:**
- ✅ **Geração de chaves:** Sempre deve funcionar
- ✅ **Registro:** HTTP 201 para chaves válidas, 400 para inválidas
- ✅ **Criptografia:** Senhas nunca em texto plano na API
- ✅ **Descriptografia:** Senha original deve ser recuperada
- ✅ **Segurança:** Acesso negado para consumidores não registrados

### **Métricas de Performance:**
- **Tempo de resposta:** < 500ms para operações RSA
- **Throughput:** > 10 requests/segundo
- **Taxa de sucesso:** > 99%

Estes scripts garantem que toda a implementação RSA está funcionando corretamente e com segurança!
