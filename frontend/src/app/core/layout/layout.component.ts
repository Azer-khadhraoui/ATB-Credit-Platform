import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, interval, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthService } from '../auth/auth.service';
import { resolvePhotoUrl } from '../../users/user.model';

const DEFAULT_VISUAL_IMAGE = '/images/imagecrud1.png';

const SLOGANS = [
  'Des professionnels à l\'écoute',
  'Ensemble, pour la réussite',
  'Votre partenaire crédit de confiance'
];

const SLOGAN_INTERVAL_MS = 4000;

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
  private readonly authService = inject(AuthService);

  sidebarOpen = signal(false);
  creditFilesExpanded = signal(true);
  clientsExpanded = signal(true);
  menuOpen = signal(false);

  readonly currentUser = this.authService.currentUser;
  readonly displayName = computed(() => this.currentUser()?.fullName ?? 'Utilisateur');
  readonly displayRole = computed(() =>
    this.currentUser()?.role === 'ADMIN' ? 'Administrateur' : 'Agent de crédit'
  );
  readonly initials = computed(() => {
    const name = this.currentUser()?.fullName?.trim();
    if (!name) {
      return 'U';
    }
    const parts = name.split(/\s+/);
    const first = parts[0]?.charAt(0) ?? '';
    const last = parts.length > 1 ? parts[parts.length - 1].charAt(0) : '';
    return (first + last).toUpperCase();
  });

  readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');
  readonly photoUrl = computed(() => resolvePhotoUrl(this.currentUser()?.photoUrl));
  readonly firstName = computed(() => this.currentUser()?.fullName?.trim().split(/\s+/)[0] ?? '');

  readonly greeting = computed(() => {
    const hour = new Date().getHours();
    return hour >= 5 && hour < 18 ? 'Bonjour' : 'Bonsoir';
  });

  private readonly sloganTick = toSignal(interval(SLOGAN_INTERVAL_MS).pipe(startWith(-1)), { initialValue: -1 });
  readonly slogan = computed(() => SLOGANS[(this.sloganTick() + 1) % SLOGANS.length]);

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  logout(): void {
    this.menuOpen.set(false);
    this.authService.logout();
    this.router.navigate(['/login']);
  }

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
