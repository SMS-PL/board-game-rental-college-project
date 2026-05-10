export type GameTag =
  | 'STRATEGY' | 'FAMILY' | 'PARTY' | 'COOPERATIVE' | 'DEXTERITY'
  | 'CARD' | 'DICE' | 'ECONOMIC' | 'ABSTRACT' | 'THEMATIC';

export const GAME_TAG_LABELS: Record<GameTag, string> = {
  STRATEGY: 'Strategia', FAMILY: 'Rodzinna', PARTY: 'Imprezowa',
  COOPERATIVE: 'Kooperacyjna', DEXTERITY: 'Zręcznościowa', CARD: 'Karciana',
  DICE: 'Kościana', ECONOMIC: 'Ekonomiczna', ABSTRACT: 'Abstrakcyjna', THEMATIC: 'Tematyczna'
};

export const ALL_GAME_TAGS: GameTag[] = [
  'STRATEGY', 'FAMILY', 'PARTY', 'COOPERATIVE', 'DEXTERITY',
  'CARD', 'DICE', 'ECONOMIC', 'ABSTRACT', 'THEMATIC'
];

export type CopyCondition = 'NEW' | 'GOOD' | 'WORN' | 'DAMAGED';

export const CONDITION_LABELS: Record<CopyCondition, string> = {
  NEW: 'Nowy', GOOD: 'Dobry', WORN: 'Zużyty', DAMAGED: 'Uszkodzony'
};

export interface GameCopy {
  id: string;
  copyNumber: number;
  condition: CopyCondition;
  isAvailable: boolean;
}

export interface Game {
  id: string;
  title: string;
  description: string;
  tags: GameTag[];
  totalCopies: number;
  availableCopies: number;
  createdAt: string;
}

export interface GameDetail {
  id: string;
  title: string;
  description: string;
  tags: GameTag[];
  totalCopies: number;
  copies: GameCopy[];
  createdAt: string;
}

export interface GameRequest {
  title: string;
  description: string;
  tags: GameTag[];
  totalCopies: number;
}

export interface UpdateGameRequest {
  title: string;
  description: string;
  tags: GameTag[];
}

export interface UpdateCopiesRequest {
  totalCopies: number;
}
