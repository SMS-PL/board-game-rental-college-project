// Copy management is done via GameService (PATCH /api/games/{id}/copies)
// GameCopy is defined in game.model.ts — re-exported here for backward compatibility
export type { GameCopy as Copy, GameCopy, CopyCondition } from './game.model';
export { CONDITION_LABELS } from './game.model';
