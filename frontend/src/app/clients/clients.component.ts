import { Component } from '@angular/core';

@Component({
  selector: 'app-clients',
  standalone: true,
  template: `
    <div class="placeholder">
      <i class="fa-solid fa-id-card"></i>
      <h1>Clients</h1>
      <p>Ce module sera construit ensuite : ajout, consultation et historique des dossiers des clients.</p>
    </div>
  `,
  styleUrl: './clients.component.scss'
})
export class ClientsComponent {}
