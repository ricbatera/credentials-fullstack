import { Routes } from '@angular/router';
import { Robots } from './components/robots/robots';
import { Consumers } from './components/consumers/consumers';
import { Credentials } from './components/credentials/credentials';
import { Configs } from './components/configs/configs';

export const routes: Routes = [
  { path: '', redirectTo: '/consumers', pathMatch: 'full' },
  { path: 'consumers', component: Consumers },
  { path: 'robots', component: Robots },
  { path: 'credentials', component: Credentials },
  { path: 'configs', component: Configs }
];
