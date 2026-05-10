export interface StatsSummary {
  totalGames: number;
  totalCopies: number;
  totalCustomers: number;
  activeRentals: number;
}

export interface PopularGame {
  gameId: string;
  title: string;
  rentalCount: number;
}
