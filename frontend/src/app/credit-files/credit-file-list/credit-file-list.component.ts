import { Component } from '@angular/core';

@Component({
  selector: 'app-credit-file-list',
  standalone: true,
  template: `
    <div class="placeholder">
      <i class="fa-solid fa-folder-open"></i>
      <h1>Liste des dossiers</h1>
      <p>Ce module sera construit ensuite : recherche, filtrage et consultation des dossiers de crédit.</p>
    </div>
  `,
  styleUrl: './credit-file-list.component.scss'
})
export class CreditFileListComponent {}
