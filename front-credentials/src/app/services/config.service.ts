
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface AppConfig {
    API_BASE_URL_CREDENTIALS: string;
    API_BASE_URL_CONSUMER_KEY: string;
    CONSUMER_IDENTIFIER: string;
    PRIVATE_KEY: string;
    PUBLIC_KEY: string;
    AMBIENTE?: string;
}

@Injectable({
    providedIn: 'root'
})
export class ConfigService {
    private config: AppConfig = {
        API_BASE_URL_CREDENTIALS: '',
        API_BASE_URL_CONSUMER_KEY: '',
        CONSUMER_IDENTIFIER: '',
        PRIVATE_KEY: '',
        PUBLIC_KEY: '',
        AMBIENTE: ''
    };
    private configLoaded = false;

    constructor(private http: HttpClient) {
        // Carrega apenas do localStorage no construtor
        this.loadConfigFromStorage();
    }

    async loadConfig(): Promise<void> {
        console.log('ConfigService: Iniciando loadConfig()');
        
        // Primeiro tenta carregar do localStorage
        this.loadConfigFromStorage();
        
        console.log('ConfigService: Configuração após localStorage:', this.config);
        
        if (!this.config.API_BASE_URL_CREDENTIALS || !this.config.CONSUMER_IDENTIFIER) {
            console.log('ConfigService: Configuração incompleta, carregando do JSON...');
            // Se não houver config válida, carrega do JSON
            try {
                console.log('ConfigService: Fazendo requisição para assets/appConfig.json');
                const json: any = await firstValueFrom(
                    this.http.get<any>('assets/appConfig.json')
                );
                console.log('ConfigService: JSON carregado:', json);
                
                // Mapeia as chaves do JSON para o objeto config
                this.config = {
                    API_BASE_URL_CREDENTIALS: json.API_BASE_URL_CREDENTIALS || '',
                    CONSUMER_IDENTIFIER: json.CONSUMER_IDENTIFIER || '',
                    PRIVATE_KEY: json.PRIVATE_KEY || '',
                    PUBLIC_KEY: json.PUBLIC_KEY || '',
                    AMBIENTE: json.AMBIENTE || '',
                    API_BASE_URL_CONSUMER_KEY: json.API_BASE_URL_CONSUMER_KEY || ''
                };
                console.log('ConfigService: Configuração final:', this.config);
                this.saveConfigToStorage();
            } catch (e) {
                console.error('Erro ao carregar appConfig.json:', e);
            }
        } else {
            console.log('ConfigService: Configuração válida encontrada no localStorage');
        }
        this.configLoaded = true;
        console.log('ConfigService: loadConfig() concluído');
    }

    getConfig(): AppConfig {
        return { ...this.config };
    }

    updateConfig(newConfig: Partial<AppConfig>): void {
        this.config = { ...this.config, ...newConfig };
        this.saveConfigToStorage();
    }

    private loadConfigFromStorage(): void {
        console.log('ConfigService: Tentando carregar do localStorage...');
        try {
            const storedConfig = localStorage.getItem('appConfig');
            if (storedConfig) {
                console.log('ConfigService: Configuração encontrada no localStorage:', storedConfig);
                const parsed = JSON.parse(storedConfig);
                this.config = { ...this.config, ...parsed };
                console.log('ConfigService: Configuração carregada do localStorage:', this.config);
            } else {
                console.log('ConfigService: Nenhuma configuração encontrada no localStorage');
            }
        } catch (e) {
            console.error('Erro ao carregar configuração do localStorage:', e);
        }
    }

    private saveConfigToStorage(): void {
        try {
            localStorage.setItem('appConfig', JSON.stringify(this.config));
        } catch (e) {
            console.error('Erro ao salvar configuração no localStorage:', e);
        }
    }

    // Métodos para configurações específicas
    getApiBaseUrlCredentials(): string {
        return this.config.API_BASE_URL_CREDENTIALS;
    }

    getApiBaseUrlConsumerKey(): string {
        return this.config.API_BASE_URL_CONSUMER_KEY;
    }

    getConsumerIdentifier(): string {
        return this.config.CONSUMER_IDENTIFIER;
    }

    getPrivateKey(): string {
        return this.config.PRIVATE_KEY;
    }

    getPublicKey(): string {
        return this.config.PUBLIC_KEY;
    }

    hasValidKeys(): boolean {
        return !!(this.config.PRIVATE_KEY && this.config.PUBLIC_KEY);
    }

    // Método para debug - limpar localStorage e recarregar
    async clearAndReload(): Promise<void> {
        console.log('ConfigService: Limpando localStorage e recarregando...');
        localStorage.removeItem('appConfig');
        this.config = {
            API_BASE_URL_CREDENTIALS: '',
            API_BASE_URL_CONSUMER_KEY: '',
            CONSUMER_IDENTIFIER: '',
            PRIVATE_KEY: '',
            PUBLIC_KEY: '',
            AMBIENTE: ''
        };
        await this.loadConfig();
    }

    // Método para verificar se a configuração foi carregada
    isConfigLoaded(): boolean {
        return this.configLoaded;
    }
}
