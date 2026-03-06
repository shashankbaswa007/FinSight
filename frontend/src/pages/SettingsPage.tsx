import { useEffect, useState } from 'react';
import { User, Lock, Save } from 'lucide-react';
import { profileApi } from '../api/profile';
import type { ProfileResponse } from '../types';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';

export default function SettingsPage() {
  const { toast } = useToast();
  const { user: authUser, login: updateAuthUser } = useAuth();
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const [nameForm, setNameForm] = useState({ name: '', email: '' });
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPw, setSavingPw] = useState(false);

  useEffect(() => {
    profileApi.get().then((p) => {
      setProfile(p);
      setNameForm({ name: p.name, email: p.email });
    }).catch(() => toast('error', 'Failed to load profile'))
      .finally(() => setLoading(false));
  }, []);

  async function handleProfileUpdate(e: React.FormEvent) {
    e.preventDefault();
    setSavingProfile(true);
    try {
      const updated = await profileApi.update({ name: nameForm.name, email: nameForm.email });
      setProfile(updated);
      // Update auth context
      if (authUser) {
        updateAuthUser({
          token: localStorage.getItem('token') || '',
          tokenType: 'Bearer',
          userId: updated.userId,
          email: updated.email,
          name: updated.name,
          role: updated.role,
        });
      }
      toast('success', 'Profile updated');
    } catch {
      toast('error', 'Failed to update profile');
    } finally { setSavingProfile(false); }
  }

  async function handlePasswordChange(e: React.FormEvent) {
    e.preventDefault();
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      toast('error', 'Passwords do not match');
      return;
    }
    setSavingPw(true);
    try {
      await profileApi.changePassword({ currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword });
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      toast('success', 'Password changed');
    } catch {
      toast('error', 'Failed to change password. Check current password.');
    } finally { setSavingPw(false); }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="h-8 w-8 border-4 border-brand-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6 page-enter max-w-2xl">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Settings</h1>
        <p className="text-gray-500 dark:text-slate-400 mt-1">Manage your account settings</p>
      </div>

      {/* Profile section */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <User className="h-5 w-5 text-brand-600" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Profile</h3>
        </div>
        <form onSubmit={handleProfileUpdate} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Name</label>
            <input type="text" required minLength={2} className="input-field" value={nameForm.name}
              onChange={(e) => setNameForm({ ...nameForm, name: e.target.value })} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Email</label>
            <input type="email" required className="input-field" value={nameForm.email}
              onChange={(e) => setNameForm({ ...nameForm, email: e.target.value })} />
          </div>
          <div className="text-xs text-gray-400 dark:text-slate-500">
            Member since: {profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : '—'}
          </div>
          <div className="flex justify-end">
            <button type="submit" disabled={savingProfile} className="btn-primary flex items-center gap-2">
              <Save className="h-4 w-4" /> {savingProfile ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>

      {/* Password section */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <Lock className="h-5 w-5 text-brand-600" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Change Password</h3>
        </div>
        <form onSubmit={handlePasswordChange} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Current Password</label>
            <input type="password" required className="input-field" value={pwForm.currentPassword}
              onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">New Password</label>
            <input type="password" required minLength={8} className="input-field" value={pwForm.newPassword}
              onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })} />
            <p className="text-xs text-gray-400 mt-1">Min 8 chars. Must include uppercase, lowercase, digit, and special character.</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Confirm New Password</label>
            <input type="password" required className="input-field" value={pwForm.confirmPassword}
              onChange={(e) => setPwForm({ ...pwForm, confirmPassword: e.target.value })} />
          </div>
          <div className="flex justify-end">
            <button type="submit" disabled={savingPw} className="btn-primary flex items-center gap-2">
              <Lock className="h-4 w-4" /> {savingPw ? 'Changing…' : 'Change Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
