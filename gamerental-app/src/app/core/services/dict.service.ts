import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { ALL_GAME_TAGS, GAME_TAG_LABELS, GameTag } from '../models/game.model';

export interface TagEntry {
  id: number;
  code: GameTag;
  name: string;
}

// Backend has no /dictionaries/categories endpoint.
// Game "categories" are replaced by the GameTag enum (managed on the backend).
// This service returns GameTag values as a static read-only list.
@Injectable({ providedIn: 'root' })
export class DictService {
  getCategories(): Observable<TagEntry[]> {
    return of(ALL_GAME_TAGS.map((tag, i) => ({ id: i + 1, code: tag, name: GAME_TAG_LABELS[tag] })));
  }

  // Write operations are not supported — GameTag is a backend enum
  createCategory(_req: any): Observable<never> {
    return new Observable(sub =>
      sub.error({ error: { detail: 'Tagi gier są zarządzane przez backend i nie można ich edytować.' } })
    );
  }

  updateCategory(_id: number, _req: any): Observable<never> {
    return new Observable(sub =>
      sub.error({ error: { detail: 'Tagi gier są zarządzane przez backend i nie można ich edytować.' } })
    );
  }

  deleteCategory(_id: number): Observable<never> {
    return new Observable(sub =>
      sub.error({ error: { detail: 'Tagi gier są zarządzane przez backend i nie można ich edytować.' } })
    );
  }
}
