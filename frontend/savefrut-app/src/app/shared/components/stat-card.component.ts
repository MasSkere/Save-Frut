import { Component, input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  template: `
    <div class="bg-surface-card rounded-card shadow-soft p-4 border-l-4" [class]="borde()">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">{{ titulo() }}</p>
      <p class="text-2xl font-bold text-gray-900 mt-1 tabular-nums">{{ valor() }}</p>
      @if (detalle()) {
        <p class="text-xs text-gray-500 mt-1">{{ detalle() }}</p>
      }
    </div>
  `
})
export class StatCardComponent {
  readonly titulo = input.required<string>();
  readonly valor = input.required<string | number>();
  readonly detalle = input<string>('');
  readonly borde = input<string>('border-brand-500');
}
