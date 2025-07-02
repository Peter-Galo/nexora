// home.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventoryComponent } from '../inventory/inventory.component';
import { WelcomeComponent } from '../welcome/welcome.component';
import { AuthService } from '../../auth/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, InventoryComponent, WelcomeComponent],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  isAuthenticated = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.authService.isAuthenticated$.subscribe(
      (auth) => (this.isAuthenticated = auth),
    );
  }
}
