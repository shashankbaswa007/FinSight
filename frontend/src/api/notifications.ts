import api from './axios';
import type { NotificationPreferencesResponse, UpdateNotificationPreferencesRequest } from '../types';

export const notificationsApi = {
  getPreferences: () =>
    api.get<NotificationPreferencesResponse>('/notifications/preferences').then((r) => r.data),
  updatePreferences: (data: UpdateNotificationPreferencesRequest) =>
    api.put<NotificationPreferencesResponse>('/notifications/preferences', data).then((r) => r.data),
};
