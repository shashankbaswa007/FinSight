import api from './axios';

export const telegramApi = {
  generateLinkCode: () =>
    api.post<{ code: string; botUsername: string }>('/telegram/generate-link-code').then((r) => r.data),
};
