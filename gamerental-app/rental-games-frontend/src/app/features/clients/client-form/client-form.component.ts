import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClientsService } from '../clients.service';

@Component({
  selector: 'app-client-form',
  standalone: false,
  templateUrl: './client-form.component.html',
  styleUrl: './client-form.component.scss'
})
export class ClientFormComponent implements OnInit {
  protected form: FormGroup;
  protected isEditMode = false;
  protected clientId: string | null = null;
  protected isLoading = false;
  protected isSaving = false;
  protected errorMessage = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly clientsService: ClientsService
  ) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phone: [''],
      email: ['', Validators.email]
    });
  }

  ngOnInit(): void {
    this.clientId = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!this.clientId;

    if (this.isEditMode && this.clientId) {
      this.isLoading = true;
      this.clientsService.getClient(this.clientId).subscribe({
        next: client => {
          this.form.patchValue(client);
          this.isLoading = false;
        },
        error: () => { this.isLoading = false; }
      });
    }
  }

  protected submit(): void {
    if (this.form.invalid) return;

    this.isSaving = true;
    this.errorMessage = '';

    const request = this.form.value;

    const action = this.isEditMode && this.clientId
      ? this.clientsService.updateClient(this.clientId, request)
      : this.clientsService.createClient(request);

    action.subscribe({
      next: client => this.router.navigate(['/clients', client.id]),
      error: () => {
        this.errorMessage = 'Nie udało się zapisać klienta.';
        this.isSaving = false;
      }
    });
  }
}

