export enum GameTag {
  STRATEGY = 'STRATEGY',
  FAMILY = 'FAMILY',
  PARTY = 'PARTY',
  COOPERATIVE = 'COOPERATIVE',
  DEXTERITY = 'DEXTERITY',
  CARD = 'CARD',
  DICE = 'DICE',
  ECONOMIC = 'ECONOMIC',
  ABSTRACT = 'ABSTRACT',
  THEMATIC = 'THEMATIC'
}

export enum CopyCondition {
  NEW = 'NEW',
  GOOD = 'GOOD',
  WORN = 'WORN',
  DAMAGED = 'DAMAGED'
}

export interface Game {
  id: string;
  title: string;
  description: string;
  tags: GameTag[];
  totalCopies: number;
  createdAt: string;
}

export interface GameCopy {
  id: string;
  gameId: string;
  copyNumber: number;
  condition: CopyCondition;
  isAvailable: boolean;
}

export interface GameDetail extends Game {
  copies: GameCopy[];
}

export interface CreateGameRequest {
  title: string;
  description: string;
  tags: GameTag[];
  totalCopies: number;
}

export interface UpdateGameRequest {
  title: string;
  description: string;
  tags: GameTag[];
  totalCopies: number;
}

