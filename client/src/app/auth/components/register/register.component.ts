import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  readonly registerForm: FormGroup;
  errorMessage = '';
  fieldErrors: { [key: string]: string } = {};
  isLoading = false;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.registerForm = this.formBuilder.group({
      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50),
        ],
      ],
      lastName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50),
        ],
      ],
      email: ['', [Validators.required, Validators.email]],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.pattern(
            '^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$',
          ),
        ],
      ],
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.fieldErrors = {};
    this.registerForm.disable();

    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/inventory']);
      },
      error: (error) => {
        this.isLoading = false;
        this.registerForm.enable();

        // Try to extract messages from both error.error and error
        const backend = error?.error || error;
        if (backend && backend.messages && Array.isArray(backend.messages)) {
          this.errorMessage = backend.messages.join(' ');
        } else if (backend && typeof backend === 'object') {
          this.fieldErrors = backend;
          this.errorMessage =
            backend.error || backend.message || 'Please correct the errors below.';
        } else {
          this.errorMessage =
            error.message || 'An error occurred during registration';
        }
      },
    });
  }

  getFieldError(field: string): string | null {
    // Show backend error if present, else show Angular validation error
    if (this.fieldErrors[field]) {
      return this.fieldErrors[field];
    }
    const control = this.registerForm.get(field);
    if (control && control.touched && control.invalid) {
      if (control.errors?.['required']) return 'This field is required';
      if (control.errors?.['minlength'])
        return `Minimum length is ${control.errors['minlength'].requiredLength}`;
      if (control.errors?.['maxlength'])
        return `Maximum length is ${control.errors['maxlength'].requiredLength}`;
      if (control.errors?.['email']) return 'Please enter a valid email';
      if (control.errors?.['pattern'])
        return 'Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and no whitespace';
    }
    return null;
  }
}
