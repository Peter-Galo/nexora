import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './inventory.component.html',
})
export class InventoryComponent {
  navItems = [
    { key: 'warehouse', label: 'Warehouse' },
    { key: 'stock', label: 'Stock' },
    { key: 'product', label: 'Product' },
    { key: 'export-jobs', label: 'Export Jobs' },
  ];

  isNavCollapsed = true;

  toggleNav() {
    this.isNavCollapsed = !this.isNavCollapsed;
  }
}
