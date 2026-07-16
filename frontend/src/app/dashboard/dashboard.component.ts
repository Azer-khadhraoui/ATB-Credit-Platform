import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="placeholder">
      <i class="fa-solid fa-gauge-high"></i>
      <h1>Tableau de bord</h1>
      <p>Ce module sera construit ensuite : statistiques des dossiers, répartition par statut et par score de risque.</p>
    </div>
  `,
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {}
