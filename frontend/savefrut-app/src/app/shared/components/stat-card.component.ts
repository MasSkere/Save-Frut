import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  template: `
    <div class="h-full bg-surface-card border border-surface-border rounded-card p-4 sm:p-5">
      <p class="flex items-center gap-2 text-sm font-medium text-gray-600">
        <span class="w-2 h-2 rounded-full shrink-0" [class]="punto()" aria-hidden="true"></span>{{ titulo() }}
      </p>
      <p class="mt-2 font-display text-2xl sm:text-3xl font-bold text-gray-900 leading-none cifra">{{ valor() }}</p>
      @if (detalle()) {
        <p class="mt-2 text-xs text-gray-600">{{ detalle() }}</p>
      }
    </div>
  `
})
export class StatCardComponent {
  readonly titulo = input.required<string>();
  readonly valor = input.required<string | number>();
  readonly detalle = input<string>('');
  /** Color de la métrica como clase de borde ("border-blue-500"); se muestra como un punto junto al título. */
  readonly borde = input<string>('border-brand-500');
  readonly punto = computed(() => this.borde().replace(/^border-/, 'bg-'));
}
