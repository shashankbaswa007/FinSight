import axios from 'axios';

// Note: CorrelationContext is set up at the app level, we store correlation IDs
// in sessionStorage so they're accessible across the entire app
const CORRELATION_ID_KEY = 'X-Request-ID';

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Capture correlation ID from responses and handle 401 responses
api.interceptors.response.use(
  (response) => {
    // Capture and store correlation ID from response header
    const correlationId = response.headers[CORRELATION_ID_KEY.toLowerCase()];
    if (correlationId) {
      sessionStorage.setItem(CORRELATION_ID_KEY, correlationId);
    }
    return response;
  },
  (error) => {
    // Capture correlation ID even on error responses
    const correlationId = error.response?.headers[CORRELATION_ID_KEY.toLowerCase()];
    if (correlationId) {
      sessionStorage.setItem(CORRELATION_ID_KEY, correlationId);
    }

    // Handle 401 responses (expired token)
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Get the most recent correlation ID for the current session.
 * Useful for displaying in error messages or support interfaces.
 */
export const getCorrelationId = (): string | null => {
  return sessionStorage.getItem(CORRELATION_ID_KEY);
};

export default api;
