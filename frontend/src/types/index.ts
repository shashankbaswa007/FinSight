/* ───── Enums ───── */
export type TransactionType = 'INCOME' | 'EXPENSE';
export type Role = 'USER' | 'ADMIN';

/* ───── Auth ───── */
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  email: string;
  name: string;
  role: string;
}

/* ───── Category ───── */
export interface CategoryRequest {
  name: string;
  type: TransactionType;
}

export interface CategoryResponse {
  id: number;
  name: string;
  type: TransactionType;
}

/* ───── Transaction ───── */
export interface TransactionRequest {
  amount: number;
  type: TransactionType;
  categoryId: number;
  description?: string;
  date: string;
}

export interface TransactionResponse {
  id: number;
  amount: number;
  type: TransactionType;
  categoryId: number;
  categoryName: string;
  description: string;
  date: string;
  createdAt: string;
}

/* ───── Budget ───── */
export interface BudgetRequest {
  categoryId: number;
  monthlyLimit: number;
  month: number;
  year: number;
}

export interface BudgetResponse {
  id: number;
  categoryId: number;
  categoryName: string;
  monthlyLimit: number;
  month: number;
  year: number;
}

export interface BudgetStatusResponse {
  budgetId: number;
  categoryName: string;
  monthlyLimit: number;
  amountSpent: number;
  remaining: number;
  exceeded: boolean;
  month: number;
  year: number;
}

/* ───── Analytics ───── */
export interface MonthlySummaryResponse {
  month: number;
  year: number;
  totalIncome: number;
  totalExpense: number;
  netSavings: number;
  incomeExpenseRatio: number;
}

export interface TopCategoryResponse {
  categoryName: string;
  totalAmount: number;
  transactionCount: number;
}

export interface SpendingTrendResponse {
  month: number;
  year: number;
  totalSpending: number;
  totalIncome: number;
}

export interface AnomalyResponse {
  transactionId: number;
  amount: number;
  categoryName: string;
  description: string;
  date: string;
  zScore: number;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
}

/* ───── Pagination ───── */
export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
