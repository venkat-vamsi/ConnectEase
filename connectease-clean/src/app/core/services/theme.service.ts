import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly storageKey = 'ce-theme';
  readonly theme = signal<Theme>(this.initial());

  constructor() {
    effect(() => {
      const t = this.theme();
      const root = document.documentElement;
      root.setAttribute('data-theme', t);
      try { localStorage.setItem(this.storageKey, t); } catch {}
    });
  }

  toggle() {
    this.theme.update(t => (t === 'dark' ? 'light' : 'dark'));
  }

  set(theme: Theme) {
    this.theme.set(theme);
  }

  private initial(): Theme {
    try {
      const saved = localStorage.getItem(this.storageKey) as Theme | null;
      if (saved === 'light' || saved === 'dark') return saved;
    } catch {}
    if (typeof window !== 'undefined' && window.matchMedia?.('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    }
    return 'light';
  }
}
