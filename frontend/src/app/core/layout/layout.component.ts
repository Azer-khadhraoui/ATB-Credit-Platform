import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {
  sidebarOpen = signal(false);
  creditFilesExpanded = signal(true);

  onSidebarEnter(): void {
    this.sidebarOpen.set(true);
  }

  onSidebarLeave(): void {
    this.sidebarOpen.set(false);
  }

  toggleCreditFiles(): void {
    if (!this.sidebarOpen()) {
      return;
    }
    this.creditFilesExpanded.update((expanded) => !expanded);
  }
}
