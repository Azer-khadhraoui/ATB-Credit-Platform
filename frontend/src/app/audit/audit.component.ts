import { Component } from '@angular/core';

@Component({
  selector: 'app-audit',
  standalone: true,
  template: `
    <div class="placeholder">
      <i class="fa-solid fa-clipboard-list"></i>
      <h1>Audit Logs</h1>
      <p>Ce module sera construit ensuite : journal des connexions, créations, modifications et analyses IA.</p>
    </div>
  `,
  styleUrl: './audit.component.scss'
})
export class AuditComponent {}
