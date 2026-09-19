import 'zone.js';
import '@angular/compiler';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule } from './app/app.module';
import './styles.css';

platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .catch((error) => console.error(error));