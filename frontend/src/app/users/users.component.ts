import { Component } from '@angular/core';

@Component({
  selector: 'app-users',
  standalone: true,
  template: `
    <div class="placeholder">
      <i class="fa-solid fa-users"></i>
      <h1>Utilisateurs</h1>
      <p>Ce module sera construit ensuite : gestion des comptes administrateurs et agents bancaires.</p>
    </div>
  `,
  styleUrl: './users.component.scss'
})
export class UsersComponent {}
