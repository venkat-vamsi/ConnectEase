import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

console.log('🏎️ IGNITION SEQUENCE STARTED'); // If you don't see this, JS isn't loading

try {
  bootstrapApplication(AppComponent, appConfig)
    .then(() => console.log('✅ App bootstrapped successfully!'))
    .catch((err) => {
      console.error('❌ Bootstrap error:', err);
      document.body.innerHTML = `<pre style="color: red; font-family: monospace; padding: 20px;">ERROR: ${err.message}\n\n${err.stack}</pre>`;
    });
} catch (err) {
  console.error('❌ Sync error:', err);
  document.body.innerHTML = `<pre style="color: red; font-family: monospace; padding: 20px;">SYNC ERROR: ${err}</pre>`;
}