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

/* ───── Recurring Transactions ───── */
export type RecurringFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';

export interface RecurringTransactionRequest {
  amount: number;
  type: TransactionType;
  categoryId: number;
  description?: string;
  frequency: RecurringFrequency;
  startDate: string;
  endDate?: string;
}

export interface RecurringTransactionResponse {
  id: number;
  amount: number;
  type: TransactionType;
  categoryId: number;
  categoryName: string;
  description: string;
  frequency: RecurringFrequency;
  startDate: string;
  endDate: string | null;
  nextOccurrence: string;
  active: boolean;
}

/* ───── Profile ───── */
export interface ProfileResponse {
  userId: number;
  name: string;
  email: string;
  role: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  name?: string;
  email?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

/* ───── Month-over-Month ───── */
export interface MonthOverMonthResponse {
  currentMonth: number;
  currentYear: number;
  currentIncome: number;
  currentExpense: number;
  previousIncome: number;
  previousExpense: number;
  incomeChange: number;
  expenseChange: number;
  incomeChangePercent: number;
  expenseChangePercent: number;
}

/* ───── Advanced Analytics ───── */
export interface DailySpendingResponse {
  date: string;
  amount: number;
}

export interface CategoryTrendResponse {
  month: number;
  year: number;
  categoryName: string;
  totalAmount: number;
}

export interface ExpenseDistributionResponse {
  range: string;
  count: number;
  totalAmount: number;
}

export interface TopDescriptionResponse {
  description: string;
  totalAmount: number;
  count: number;
}
