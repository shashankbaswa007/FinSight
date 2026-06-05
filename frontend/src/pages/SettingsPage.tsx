import { useEffect, useState } from 'react';
import { Bell, Lock, Save, User } from 'lucide-react';
import { profileApi } from '../api/profile';
import { reconciliationApi } from '../api/reconciliation';
import { notificationsApi } from '../api/notifications';
import { getCorrelationId } from '../api/axios';
import type {
  NotificationPreferencesResponse,
  ProfileResponse,
  ReconciliationScheduleSettingsResponse,
} from '../types';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';

export default function SettingsPage() {
  const { toast } = useToast();
  const { user: authUser, login: updateAuthUser } = useAuth();
  const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [scheduleSettings, setScheduleSettings] = useState<ReconciliationScheduleSettingsResponse | null>(null);
  const [prefsForm, setPrefsForm] = useState<NotificationPreferencesResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const [nameForm, setNameForm] = useState({ name: '', email: '' });
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPw, setSavingPw] = useState(false);
  const [savingSchedule, setSavingSchedule] = useState(false);
  const [savingPrefs, setSavingPrefs] = useState(false);

  useEffect(() => {
    let cancelled = false;
    Promise.all([profileApi.get(), reconciliationApi.getScheduleSettings(), notificationsApi.getPreferences()])
      .then(([p, schedule, prefs]) => {
        if (cancelled) return;
        setProfile(p);
        setNameForm({ name: p.name, email: p.email });
        setScheduleSettings(schedule);
        setPrefsForm(prefs);
      })
      .catch(() => {
        toast('error', 'Failed to load settings', getCorrelationId() || undefined);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
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

  async function handleScheduleToggle() {
    if (!scheduleSettings) return;
    setSavingSchedule(true);
    try {
      const updated = await reconciliationApi.updateScheduleSettings({
        enabled: !scheduleSettings.enabled,
      });
      setScheduleSettings(updated);
      toast('success', updated.enabled ? 'Scheduled reconciliation enabled' : 'Scheduled reconciliation disabled');
    } catch {
      toast('error', 'Failed to update scheduled reconciliation', getCorrelationId() || undefined);
    } finally {
      setSavingSchedule(false);
    }
  }

  async function handlePreferencesSave(e: React.FormEvent) {
    e.preventDefault();
    if (!prefsForm) return;
    setSavingPrefs(true);
    try {
      const updated = await notificationsApi.updatePreferences(prefsForm);
      setPrefsForm(updated);
      toast('success', 'Notification preferences updated');
    } catch {
      toast('error', 'Failed to update notification preferences', getCorrelationId() || undefined);
    } finally {
      setSavingPrefs(false);
    }
  }

  function updatePrefs<K extends keyof NotificationPreferencesResponse>(
    key: K,
    value: NotificationPreferencesResponse[K]
  ) {
    setPrefsForm((prev) => (prev ? { ...prev, [key]: value } : prev));
  }

  function handleThresholdChange(value: string) {
    if (!prefsForm) return;
    const parsed = Number(value);
    if (Number.isNaN(parsed)) return;
    const clamped = Math.min(100, Math.max(1, Math.round(parsed)));
    updatePrefs('budgetAlertThreshold', clamped);
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

      {/* Scheduled reconciliation section */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <Bell className="h-5 w-5 text-brand-600" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Notification Preferences</h3>
        </div>
        {prefsForm ? (
          <form onSubmit={handlePreferencesSave} className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-medium text-gray-800 dark:text-slate-200">Budget alerts</p>
                  <p className="text-xs text-gray-400 dark:text-slate-500">Receive alerts when spending crosses the threshold.</p>
                </div>
                <button
                  type="button"
                  onClick={() => updatePrefs('budgetAlertsEnabled', !prefsForm.budgetAlertsEnabled)}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    prefsForm.budgetAlertsEnabled ? 'bg-brand-600' : 'bg-gray-300 dark:bg-slate-600'
                  }`}
                  aria-pressed={prefsForm.budgetAlertsEnabled}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                      prefsForm.budgetAlertsEnabled ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-medium text-gray-800 dark:text-slate-200">In-app notifications</p>
                  <p className="text-xs text-gray-400 dark:text-slate-500">Show alerts in your notification center.</p>
                </div>
                <button
                  type="button"
                  onClick={() => updatePrefs('alertInApp', !prefsForm.alertInApp)}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    prefsForm.alertInApp ? 'bg-brand-600' : 'bg-gray-300 dark:bg-slate-600'
                  }`}
                  aria-pressed={prefsForm.alertInApp}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                      prefsForm.alertInApp ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-medium text-gray-800 dark:text-slate-200">Email digests</p>
                  <p className="text-xs text-gray-400 dark:text-slate-500">Send a daily or weekly email summary.</p>
                </div>
                <button
                  type="button"
                  onClick={() => updatePrefs('alertEmail', !prefsForm.alertEmail)}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    prefsForm.alertEmail ? 'bg-brand-600' : 'bg-gray-300 dark:bg-slate-600'
                  }`}
                  aria-pressed={prefsForm.alertEmail}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                      prefsForm.alertEmail ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">
                  Budget alert threshold (%)
                </label>
                <input
                  type="number"
                  min={1}
                  max={100}
                  className="input-field"
                  value={prefsForm.budgetAlertThreshold}
                  onChange={(e) => handleThresholdChange(e.target.value)}
                />
                <p className="text-xs text-gray-400 dark:text-slate-500 mt-1">
                  Alert when spending crosses this percentage of the budget.
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">
                  Email digest frequency
                </label>
                <select
                  className="input-field"
                  value={prefsForm.alertFrequency}
                  onChange={(e) => updatePrefs('alertFrequency', e.target.value as NotificationPreferencesResponse['alertFrequency'])}
                  disabled={!prefsForm.alertEmail}
                >
                  <option value="REAL_TIME">Real-time only</option>
                  <option value="DAILY">Daily digest</option>
                  <option value="WEEKLY">Weekly digest</option>
                </select>
                <p className="text-xs text-gray-400 dark:text-slate-500 mt-1">
                  Digests send at 08:00 server time when enabled.
                </p>
              </div>
            </div>

            <div className="flex justify-end">
              <button type="submit" disabled={savingPrefs} className="btn-primary flex items-center gap-2">
                <Save className="h-4 w-4" /> {savingPrefs ? 'Saving...' : 'Save Preferences'}
              </button>
            </div>
          </form>
        ) : (
          <p className="text-sm text-gray-500 dark:text-slate-400">Notification preferences are unavailable.</p>
        )}
      </div>

      {/* Scheduled reconciliation section */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <span className="h-2 w-2 rounded-full bg-emerald-500" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Scheduled Reconciliation</h3>
        </div>
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm text-gray-700 dark:text-slate-300">
              Automatically reconcile your transactions each day.
            </p>
            <p className="text-xs text-gray-400 dark:text-slate-500 mt-1">
              Runs by server schedule: {scheduleSettings?.cron || '—'}
            </p>
            <p className="text-xs text-gray-400 dark:text-slate-500 mt-1">
              Your time zone: {localTimeZone}
            </p>
            {!scheduleSettings?.globalEnabled && (
              <p className="text-xs text-amber-600 dark:text-amber-400 mt-1">
                Scheduler is disabled globally by the server.
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={handleScheduleToggle}
            disabled={savingSchedule || !scheduleSettings?.globalEnabled}
            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
              scheduleSettings?.enabled ? 'bg-brand-600' : 'bg-gray-300 dark:bg-slate-600'
            } ${savingSchedule || !scheduleSettings?.globalEnabled ? 'opacity-50 cursor-not-allowed' : ''}`}
            aria-pressed={scheduleSettings?.enabled}
            aria-label="Toggle scheduled reconciliation"
          >
            <span
              className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                scheduleSettings?.enabled ? 'translate-x-6' : 'translate-x-1'
              }`}
            />
          </button>
        </div>
      </div>
    </div>
  );
}
