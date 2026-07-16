import { Component } from '@angular/core';

@Component({
  selector: 'app-settings',
  standalone: true,
  template: `
    <div class="placeholder">
      <i class="fa-solid fa-gear"></i>
      <h1>Paramètres</h1>
      <p>Ce module sera construit ensuite : préférences du compte et configuration de la plateforme.</p>
    </div>
  `,
  styleUrl: './settings.component.scss'
})
export class SettingsComponent {}
