import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Marca de SaveFrut: corte transversal de un cítrico, en el verde del puesto. */
@Component({
  selector: 'app-logo',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'inline-flex items-center gap-2' },
  template: `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" [attr.width]="tamano()" [attr.height]="tamano()" aria-hidden="true">
      <circle cx="16" cy="16" r="15" [attr.fill]="invertido() ? '#8ec98e' : '#2d5a27'" />
      <circle cx="16" cy="16" r="11.5" fill="#f3efc9" />
      <g stroke="#2d5a27" stroke-width="1.6" stroke-linecap="round">
        <path d="M16 6v20" />
        <path d="M7.34 11l17.32 10" />
        <path d="M7.34 21l17.32-10" />
      </g>
      <circle cx="16" cy="16" r="2" fill="#2d5a27" />
    </svg>
    @if (conTexto()) {
      <span class="font-display text-xl font-bold tracking-tight" [class]="invertido() ? 'text-white' : 'text-brand-700'">SaveFrut</span>
    }
  `
})
export class LogoComponent {
  readonly tamano = input(32);
  readonly conTexto = input(true);
  readonly invertido = input(false);
}
