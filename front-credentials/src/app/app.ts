import { Component, signal, OnInit, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { ConfigService } from './services/config.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('front-credentials');
  private configService = inject(ConfigService);

  ngOnInit() {
    // Log para debug da configuração
    console.log('App: Componente inicializado');
    console.log('App: Configuração atual:', this.configService.getConfig());
    console.log('App: Config carregada?', this.configService.isConfigLoaded());
    
    // Se não foi carregada, força o carregamento
    if (!this.configService.isConfigLoaded()) {
      console.log('App: Forçando carregamento da configuração...');
      this.configService.loadConfig().then(() => {
        console.log('App: Configuração carregada:', this.configService.getConfig());
      });
    }
  }
}
