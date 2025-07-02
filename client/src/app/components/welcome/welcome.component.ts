import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-welcome',
  imports: [FormsModule, RouterLink],
  templateUrl: './welcome.component.html',
})
export class WelcomeComponent {}
