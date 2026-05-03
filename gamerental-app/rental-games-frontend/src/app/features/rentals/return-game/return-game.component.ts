import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CopyCondition } from '../../../core/models/game.model';
import { Rental } from '../../../core/models/rental.model';
import { RentalsService } from '../rentals.service';

@Component({
  selector: 'app-return-game',
  standalone: false,
  templateUrl: './return-game.component.html',
  styleUrl: './return-game.component.scss'
})
export class ReturnGameComponent implements OnInit {
  protected form: FormGroup;
  protected rental: Rental | null = null;
  protected isLoading = false;
  protected isSaving = false;
  protected errorMessage = '';

  protected readonly copyConditions = Object.values(CopyCondition);

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly rentalsService: RentalsService
  ) {
    this.form = this.fb.group({
      conditionOnReturn: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isLoading = true;
      this.rentalsService.getRental(id).subscribe({
        next: rental => {
          this.rental = rental;
          this.isLoading = false;
        },
        error: () => { this.isLoading = false; }
      });
    }
  }

  protected conditionLabel(condition: CopyCondition): string {
    const labels: Record<CopyCondition, string> = {
      [CopyCondition.NEW]: 'Nowy',
      [CopyCondition.GOOD]: 'Dobry',
      [CopyCondition.WORN]: 'Zużyty',
      [CopyCondition.DAMAGED]: 'Uszkodzony'
    };
    return labels[condition];
  }

  protected submit(): void {
    if (this.form.invalid || !this.rental) return;

    this.isSaving = true;
    this.errorMessage = '';

    this.rentalsService.returnRental(this.rental.id, this.form.value).subscribe({
      next: () => this.router.navigate(['/rentals']),
      error: () => {
        this.errorMessage = 'Nie udało się zarejestrować zwrotu.';
        this.isSaving = false;
      }
    });
  }
}

