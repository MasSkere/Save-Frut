import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyPEN',
  standalone: true
})
export class CurrencyPENPipe implements PipeTransform {
  transform(value: number | null | undefined, showSymbol: boolean = true): string {
    if (value == null || isNaN(value)) {
      return showSymbol ? 'S/ 0.00' : '0.00';
    }

    const formatted = value.toLocaleString('es-PE', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });

    return showSymbol ? `S/ ${formatted}` : formatted;
  }
}