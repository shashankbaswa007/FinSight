import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';

interface CorrelationContextValue {
  correlationId: string | null;
  setCorrelationId: (id: string | null) => void;
  lastCorrelationId: string | null;
}

const CorrelationContext = createContext<CorrelationContextValue>({
  correlationId: null,
  setCorrelationId: () => {},
  lastCorrelationId: null,
});

/**
 * Hook to access the current request correlation ID.
 * 
 * The correlation ID is set by the response interceptor in axios.ts
 * whenever an API request completes. It can be used to:
 * - Display in error messages for debugging
 * - Include in support tickets
 * - Trace requests through server logs
 * 
 * Example:
 *   const { correlationId, lastCorrelationId } = useCorrelation();
 *   // correlationId is the most recent request ID
 *   // lastCorrelationId is the previous one (persists for support display)
 */
export const useCorrelation = () => useContext(CorrelationContext);

export function CorrelationProvider({ children }: { children: ReactNode }) {
  const [correlationId, setCorrelationId] = useState<string | null>(null);
  const [lastCorrelationId, setLastCorrelationId] = useState<string | null>(null);

  const handleSetCorrelationId = useCallback((id: string | null) => {
    if (id) {
      // Keep the previous ID for persistence in UI (e.g., error displays)
      setLastCorrelationId(id);
    }
    setCorrelationId(id);
  }, []);

  return (
    <CorrelationContext.Provider
      value={{
        correlationId,
        setCorrelationId: handleSetCorrelationId,
        lastCorrelationId,
      }}
    >
      {children}
    </CorrelationContext.Provider>
  );
}
