import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/auth';
import { TrendingUp, Mail, Lock, Loader2, ArrowRight, ShieldCheck, PieChart } from 'lucide-react';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await authApi.login({ email, password });
      login(data);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = async () => {
    setError('');
    setLoading(true);
    try {
      const data = await authApi.login({ email: 'demo@finsight.com', password: 'Demo@1234' });
      login(data);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex relative overflow-hidden bg-slate-50 dark:bg-slate-900 transition-colors duration-300">
      {/* Decorative ambient background glows */}
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-brand-400/20 blur-[120px] dark:bg-brand-600/10 pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-5%] w-[40%] h-[40%] rounded-full bg-indigo-400/20 blur-[120px] dark:bg-indigo-600/10 pointer-events-none" />

      {/* Left panel – Immersive Branding */}
      <div className="hidden lg:flex lg:w-5/12 relative z-10 flex-col justify-between p-12 bg-gradient-to-b from-slate-900 via-brand-950 to-slate-900 text-white shadow-2xl border-r border-white/5">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-brand-400 to-brand-600 shadow-lg shadow-brand-500/30">
            <TrendingUp className="h-6 w-6 text-white" />
          </div>
          <span className="text-3xl font-bold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-brand-200">FinSight</span>
        </div>

        <div className="space-y-6 mt-12 animate-slide-up">
          <h1 className="text-5xl font-extrabold leading-[1.1] tracking-tight">
            Financial clarity, <br/><span className="text-brand-400">redefined.</span>
          </h1>
          <p className="text-lg text-slate-300 leading-relaxed max-w-md font-light">
            Elevate your financial journey with AI-driven insights, autonomous reconciliation, and beautiful analytics.
          </p>
        </div>

        <div className="mt-auto pt-16 grid grid-cols-1 gap-4 animate-slide-up" style={{ animationDelay: '0.1s' }}>
          <div className="flex items-center gap-4 bg-white/5 backdrop-blur-md border border-white/5 rounded-2xl p-4 transition-transform hover:-translate-y-1 hover:bg-white/10">
            <div className="p-3 bg-brand-500/20 rounded-xl border border-brand-500/20"><PieChart className="text-brand-300 h-6 w-6" /></div>
            <div>
              <h3 className="font-semibold text-white">Deep Analytics</h3>
              <p className="text-sm text-slate-400">Visualize your wealth beautifully</p>
            </div>
          </div>
          <div className="flex items-center gap-4 bg-white/5 backdrop-blur-md border border-white/5 rounded-2xl p-4 transition-transform hover:-translate-y-1 hover:bg-white/10">
            <div className="p-3 bg-indigo-500/20 rounded-xl border border-indigo-500/20"><ShieldCheck className="text-indigo-300 h-6 w-6" /></div>
            <div>
              <h3 className="font-semibold text-white">Bank-grade Security</h3>
              <p className="text-sm text-slate-400">Your data is encrypted and isolated</p>
            </div>
          </div>
        </div>
      </div>

      {/* Right panel – Form */}
      <div className="flex w-full lg:w-7/12 items-center justify-center p-6 sm:p-12 relative z-10">
        <div className="w-full max-w-md animate-slide-up bg-white/60 dark:bg-slate-900/60 backdrop-blur-2xl border border-white/40 dark:border-slate-800/60 p-8 sm:p-10 rounded-3xl shadow-xl">
          <div className="lg:hidden flex items-center gap-3 mb-10 justify-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-600 shadow-lg shadow-brand-500/30">
              <TrendingUp className="h-6 w-6 text-white" />
            </div>
            <span className="text-3xl font-bold text-slate-900 dark:text-white">FinSight</span>
          </div>

          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-slate-900 dark:text-white mb-2 tracking-tight">Welcome back</h2>
            <p className="text-slate-500 dark:text-slate-400">Enter your credentials to access your dashboard</p>
          </div>

          {error && (
            <div data-testid="error-message" className="mb-6 p-4 rounded-xl bg-rose-50/80 dark:bg-rose-900/20 border border-rose-200 dark:border-rose-800/50 text-rose-600 dark:text-rose-400 text-sm flex items-start gap-3 animate-slide-down">
              <ShieldCheck className="h-5 w-5 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 ml-1">Email address</label>
              <div className="relative group">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400 group-focus-within:text-brand-500 transition-colors" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 rounded-xl bg-white/80 dark:bg-slate-950/80 border border-slate-200 dark:border-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-brand-500/50 focus:border-brand-500 outline-none transition-all placeholder:text-slate-400 shadow-sm"
                  placeholder="you@example.com"
                  required
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 ml-1">Password</label>
              <div className="relative group">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400 group-focus-within:text-brand-500 transition-colors" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 rounded-xl bg-white/80 dark:bg-slate-950/80 border border-slate-200 dark:border-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-brand-500/50 focus:border-brand-500 outline-none transition-all placeholder:text-slate-400 shadow-sm"
                  placeholder="••••••••"
                  required
                />
              </div>
            </div>

            <div className="pt-3">
              <button type="submit" disabled={loading} className="w-full bg-slate-900 hover:bg-slate-800 dark:bg-brand-600 dark:hover:bg-brand-500 text-white py-3.5 px-4 rounded-xl font-semibold transition-all flex items-center justify-center gap-2 shadow-lg shadow-slate-900/10 dark:shadow-brand-600/20 group">
                {loading ? (
                  <Loader2 className="h-5 w-5 animate-spin" />
                ) : (
                  <>Sign in <ArrowRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" /></>
                )}
              </button>
            </div>

            <div className="relative py-4 flex items-center justify-center">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-200 dark:border-slate-800"></div>
              </div>
              <span className="relative bg-transparent px-4 text-xs font-semibold text-slate-400 uppercase tracking-widest">
                Or
              </span>
            </div>

            <button
              type="button"
              onClick={handleDemoLogin}
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white/80 dark:bg-slate-800/80 hover:bg-white dark:hover:bg-slate-700 px-4 py-3 text-sm font-semibold text-slate-700 dark:text-slate-200 shadow-sm transition-all duration-200 disabled:opacity-50"
            >
              Access Demo Account
            </button>
          </form>

          <p className="mt-8 text-center text-sm text-slate-500 dark:text-slate-400">
            Don't have an account?{' '}
            <Link to="/register" className="font-semibold text-brand-600 dark:text-brand-400 hover:text-brand-500 transition-colors">
              Create one for free
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
