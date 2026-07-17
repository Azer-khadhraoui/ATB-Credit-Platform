import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

const DEFAULT_VISUAL_IMAGE = '/images/imagecrud1.png';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  sidebarOpen = signal(false);
  creditFilesExpanded = signal(true);
  clientsExpanded = signal(true);

  visualImage = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      startWith(null),
      map(() => this.resolveVisualImage(this.activatedRoute))
    ),
    { initialValue: DEFAULT_VISUAL_IMAGE }
  );

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

  toggleClients(): void {
    if (!this.sidebarOpen()) {
      return;
    }
    this.clientsExpanded.update((expanded) => !expanded);
  }

  private resolveVisualImage(route: ActivatedRoute): string {
    let current: ActivatedRoute | null = route;
    let image = DEFAULT_VISUAL_IMAGE;
    while (current) {
      const visual = current.snapshot?.data?.['visualImage'];
      if (visual) {
        image = visual;
      }
      current = current.firstChild;
    }
    return image;
  }
}
