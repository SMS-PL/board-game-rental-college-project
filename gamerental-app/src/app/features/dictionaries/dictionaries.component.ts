import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { ALL_GAME_TAGS, GAME_TAG_LABELS, GameTag } from '../../core/models/game.model';

// Categories are replaced by GameTag enum on the backend — this page shows available tags (read-only)
@Component({
  selector: 'app-dictionaries',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './dictionaries.component.html'
})
export class DictionariesComponent {
  readonly tags: { code: GameTag; name: string }[] =
    ALL_GAME_TAGS.map(tag => ({ code: tag, name: GAME_TAG_LABELS[tag] }));
}
