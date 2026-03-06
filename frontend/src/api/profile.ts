import api from './axios';
import type { ProfileResponse, UpdateProfileRequest, ChangePasswordRequest } from '../types';

export const profileApi = {
  get: () =>
    api.get<ProfileResponse>('/profile').then((r) => r.data),

  update: (data: UpdateProfileRequest) =>
    api.put<ProfileResponse>('/profile', data).then((r) => r.data),

  changePassword: (data: ChangePasswordRequest) =>
    api.put('/profile/password', data).then((r) => r.data),
};
