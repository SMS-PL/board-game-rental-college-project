import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Client } from '../../../core/models/client.model';
import { Game, GameCopy } from '../../../core/models/game.model';
import { ClientsService } from '../../clients/clients.service';
import { GamesService } from '../../games/games.service';
import { RentalsService } from '../rentals.service';

@Component({
  selector: 'app-issue-game',
  standalone: false,
  templateUrl: './issue-game.component.html',
  styleUrl: './issue-game.component.scss'
})
export class IssueGameComponent implements OnInit {
  protected form: FormGroup;
  protected clients: Client[] = [];
  protected games: Game[] = [];
  protected availableCopies: GameCopy[] = [];
  protected isLoading = false;
  protected isSaving = false;
  protected errorMessage = '';

  protected readonly today = new Date().toISOString().split('T')[0];

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly clientsService: ClientsService,
    private readonly gamesService: GamesService,
    private readonly rentalsService: RentalsService
  ) {
    this.form = this.fb.group({
      clientId: ['', Validators.required],
      gameId: ['', Validators.required],
      gameCopyId: ['', Validators.required],
      rentedFrom: [this.today, Validators.required],
      dueTo: ['', Validators.required],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.clientsService.getClients().subscribe({
      next: clients => { this.clients = clients; }
    });
    this.gamesService.getGames().subscribe({
      next: games => {
        this.games = games;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });

    this.form.get('gameId')?.valueChanges.subscribe(gameId => {
      this.availableCopies = [];
      this.form.get('gameCopyId')?.setValue('');
      if (gameId) {
        this.gamesService.getAvailableCopies(gameId).subscribe({
          next: copies => { this.availableCopies = copies; }
        });
      }
    });
  }

  protected clientLabel(client: Client): string {
    return `${client.firstName} ${client.lastName}${client.phone ? ' – ' + client.phone : ''}`;
  }

  protected submit(): void {
    if (this.form.invalid) return;

    this.isSaving = true;
    this.errorMessage = '';

    const { gameId, ...request } = this.form.value;

    this.rentalsService.createRental(request).subscribe({
      next: () => this.router.navigate(['/rentals']),
      error: () => {
        this.errorMessage = 'Nie udało się utworzyć wypożyczenia.';
        this.isSaving = false;
      }
    });
  }
}

