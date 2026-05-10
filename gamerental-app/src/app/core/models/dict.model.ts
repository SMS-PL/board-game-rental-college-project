export interface BaseDict {
  id: number;
  code: string;
  name: string;
}

export interface DictRequest {
  code: string;
  name: string;
}

export type Category = BaseDict;

