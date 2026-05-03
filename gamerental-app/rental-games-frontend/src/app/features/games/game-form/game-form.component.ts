import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GameTag } from '../../../core/models/game.model';
import { GamesService } from '../games.service';

@Component({
  selector: 'app-game-form',
  standalone: false,
  templateUrl: './game-form.component.html',
  styleUrl: './game-form.component.scss'
})
export class GameFormComponent implements OnInit {
  protected form: FormGroup;
  protected isEditMode = false;
  protected gameId: string | null = null;
  protected isLoading = false;
  protected isSaving = false;
  protected errorMessage = '';

  protected readonly gameTags = Object.values(GameTag);

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly gamesService: GamesService
  ) {
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      tags: [[]],
      totalCopies: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.gameId = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!this.gameId;

    if (this.isEditMode && this.gameId) {
      this.isLoading = true;
      this.gamesService.getGame(this.gameId).subscribe({
        next: game => {
          this.form.patchValue({
            title: game.title,
            description: game.description,
            tags: game.tags,
            totalCopies: game.totalCopies
          });
          this.isLoading = false;
        },
        error: () => { this.isLoading = false; }
      });
    }
  }

  protected isTagSelected(tag: GameTag): boolean {
    return (this.form.get('tags')?.value as GameTag[]).includes(tag);
  }

  protected toggleTag(tag: GameTag): void {
    const current: GameTag[] = this.form.get('tags')?.value ?? [];
    const updated = current.includes(tag)
      ? current.filter(t => t !== tag)
      : [...current, tag];
    this.form.get('tags')?.setValue(updated);
  }

  protected submit(): void {
    if (this.form.invalid) return;

    this.isSaving = true;
    this.errorMessage = '';

    const request = this.form.value;

    const action = this.isEditMode && this.gameId
      ? this.gamesService.updateGame(this.gameId, request)
      : this.gamesService.createGame(request);

    action.subscribe({
      next: game => this.router.navigate(['/games', game.id]),
      error: () => {
        this.errorMessage = 'Nie udało się zapisać gry.';
        this.isSaving = false;
      }
    });
  }
}

