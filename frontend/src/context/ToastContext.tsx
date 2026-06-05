import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import { CheckCircle2, XCircle, AlertTriangle, Info, X, Copy, Check } from 'lucide-react';

type ToastType = 'success' | 'error' | 'warning' | 'info';

interface Toast {
  id: number;
  type: ToastType;
  message: string;
  correlationId?: string; // Request correlation ID for debugging
}

interface ToastContextValue {
  toast: (type: ToastType, message: string, correlationId?: string) => void;
}

const ToastContext = createContext<ToastContextValue>({ toast: () => {} });

export const useToast = () => useContext(ToastContext);

let nextId = 0;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const toast = useCallback((type: ToastType, message: string, correlationId?: string) => {
    const id = ++nextId;
    setToasts((prev) => [...prev, { id, type, message, correlationId }]);
    // Errors stay longer to give users time to read correlation ID
    const duration = type === 'error' ? 6000 : 4000;
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), duration);
  }, []);

  const dismiss = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      {/* Toast container */}
      <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2 pointer-events-none">
        {toasts.map((t) => (
          <ToastItem key={t.id} toast={t} onDismiss={dismiss} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

const iconMap = {
  success: CheckCircle2,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
};

const styleMap = {
  success: 'border-emerald-200 dark:border-emerald-800 bg-emerald-50 dark:bg-emerald-900/20 text-emerald-800 dark:text-emerald-300',
  error: 'border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20 text-red-800 dark:text-red-300',
  warning: 'border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-900/20 text-amber-800 dark:text-amber-300',
  info: 'border-blue-200 dark:border-blue-800 bg-blue-50 dark:bg-blue-900/20 text-blue-800 dark:text-blue-300',
};

function ToastItem({ toast, onDismiss }: { toast: Toast; onDismiss: (id: number) => void }) {
  const Icon = iconMap[toast.type];
  const [copied, setCopied] = useState(false);

  const handleCopyCorrelationId = () => {
    if (toast.correlationId) {
      navigator.clipboard.writeText(toast.correlationId);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div
      className={`pointer-events-auto flex flex-col gap-2 min-w-[300px] max-w-md px-4 py-3 rounded-xl border shadow-lg animate-slide-up ${styleMap[toast.type]}`}
    >
      <div className="flex items-start gap-3">
        <Icon className="h-5 w-5 shrink-0 mt-0.5" />
        <p className="text-sm font-medium flex-1">{toast.message}</p>
        <button
          onClick={() => onDismiss(toast.id)}
          className="shrink-0 opacity-60 hover:opacity-100 transition-opacity"
        >
          <X className="h-4 w-4" />
        </button>
      </div>
      {/* Show correlation ID for errors */}
      {toast.correlationId && toast.type === 'error' && (
        <div className="flex items-center gap-2 text-xs opacity-80">
          <span className="font-mono bg-black/10 dark:bg-white/10 px-2 py-1 rounded flex-1 truncate">
            {toast.correlationId}
          </span>
          <button
            onClick={handleCopyCorrelationId}
            className="opacity-60 hover:opacity-100 transition-opacity p-1"
            title="Copy correlation ID"
          >
            {copied ? <Check className="h-3 w-3" /> : <Copy className="h-3 w-3" />}
          </button>
        </div>
      )}
    </div>
  );
}
