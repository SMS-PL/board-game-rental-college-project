import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  username = '';
  password = '';
  confirmPassword = '';
  loading = false;
  error = '';
  success = '';

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit(): void {
    this.error = '';
    if (this.password !== this.confirmPassword) {
      this.error = 'Hasła nie są identyczne.';
      return;
    }
    if (this.password.length < 6) {
      this.error = 'Hasło musi mieć co najmniej 6 znaków.';
      return;
    }
    this.loading = true;
    this.auth.register({ username: this.username, password: this.password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.error = err.status === 409
          ? 'Użytkownik o tej nazwie już istnieje.'
          : 'Błąd podczas rejestracji. Spróbuj ponownie.';
        this.loading = false;
      }
    });
  }
}

