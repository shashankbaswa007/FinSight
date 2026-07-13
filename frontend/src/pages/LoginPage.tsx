import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/auth';
import { TrendingUp, Mail, Lock, Loader2, ArrowRight, ShieldCheck, PieChart, Layers } from 'lucide-react';
import { motion, Variants } from 'framer-motion';

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

  const containerVariants: Variants = {
    hidden: { opacity: 0 },
    visible: { 
      opacity: 1,
      transition: { 
        staggerChildren: 0.1,
        delayChildren: 0.2
      }
    }
  };

  const itemVariants: Variants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: { type: "spring", stiffness: 300, damping: 24 }
    }
  };

  return (
    <div className="min-h-screen flex relative bg-white dark:bg-black font-sans selection:bg-accent-pink selection:text-black">
      {/* Left panel – Neo Brutalist Branding */}
      <div className="hidden lg:flex lg:w-1/2 relative z-10 flex-col justify-between p-16 bg-accent-yellow border-r-4 border-black">
        <motion.div 
          initial={{ opacity: 0, x: -30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, ease: "easeOut" }}
          className="flex items-center gap-3"
        >
          <div className="flex h-14 w-14 items-center justify-center bg-white border-4 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
            <TrendingUp className="h-8 w-8 text-black" />
          </div>
          <span className="text-4xl font-bold tracking-tight text-black uppercase border-b-4 border-transparent">FINSIGHT</span>
        </motion.div>

        <motion.div 
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2, ease: "easeOut" }}
          className="space-y-6 max-w-xl"
        >
          <h1 className="text-6xl xl:text-7xl font-black leading-[1.0] tracking-tighter text-black uppercase">
            Master your wealth with <span className="bg-accent-blue text-white px-2 mt-2 inline-block shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] border-4 border-black">brute force.</span>
          </h1>
          <p className="text-xl text-black font-bold leading-relaxed border-l-4 border-black pl-4">
            Experience the future of personal finance. No fluff, just hard numbers, autonomous reconciliation, and crystal clear analytics.
          </p>
        </motion.div>

        <motion.div 
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4, ease: "easeOut" }}
          className="grid grid-cols-2 gap-6"
        >
          <div className="flex flex-col gap-4 bg-accent-pink border-4 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] p-6 hover:-translate-y-1 hover:-translate-x-1 hover:shadow-[12px_12px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="p-3 bg-white border-2 border-black w-fit shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"><PieChart className="text-black h-8 w-8" /></div>
            <div>
              <h3 className="font-black text-black text-xl uppercase">Deep Analytics</h3>
              <p className="text-sm font-bold text-black mt-1">Visualize your financial landscape with zero BS.</p>
            </div>
          </div>
          <div className="flex flex-col gap-4 bg-accent-green border-4 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] p-6 hover:-translate-y-1 hover:-translate-x-1 hover:shadow-[12px_12px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="p-3 bg-white border-2 border-black w-fit shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"><ShieldCheck className="text-black h-8 w-8" /></div>
            <div>
              <h3 className="font-black text-black text-xl uppercase">Bank-Grade</h3>
              <p className="text-sm font-bold text-black mt-1">Your data is locked down, encrypted and isolated.</p>
            </div>
          </div>
        </motion.div>
      </div>

      {/* Right panel – Form */}
      <div className="flex w-full lg:w-1/2 items-center justify-center p-6 sm:p-12 relative z-10 bg-white dark:bg-black">
        <motion.div 
          initial="hidden"
          animate="visible"
          variants={containerVariants}
          className="w-full max-w-[440px] bg-white dark:bg-black border-4 border-black dark:border-white p-10 shadow-[12px_12px_0px_0px_rgba(0,0,0,1)] dark:shadow-[12px_12px_0px_0px_rgba(255,255,255,1)]"
        >
          <motion.div variants={itemVariants} className="lg:hidden flex items-center gap-3 mb-12 justify-center">
            <div className="flex h-12 w-12 items-center justify-center bg-accent-yellow border-4 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
              <TrendingUp className="h-6 w-6 text-black" />
            </div>
            <span className="text-4xl font-black text-black dark:text-white tracking-tighter uppercase">FinSight</span>
          </motion.div>

          <motion.div variants={itemVariants} className="mb-10 text-center lg:text-left">
            <h2 className="text-4xl font-black text-black dark:text-white mb-3 tracking-tighter uppercase">Enter.</h2>
            <p className="text-black dark:text-white font-bold bg-accent-blue inline-block px-2 py-1 border-2 border-black">Access your workspace</p>
          </motion.div>

          {error && (
            <motion.div 
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-8 p-4 bg-accent-red border-4 border-black text-black text-sm flex items-start gap-3 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] font-bold"
            >
              <ShieldCheck className="h-6 w-6 shrink-0" />
              <span>{error}</span>
            </motion.div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <motion.div variants={itemVariants} className="space-y-2">
              <label className="block text-sm font-black text-black dark:text-white uppercase tracking-wider">Email address</label>
              <div className="relative group">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 h-6 w-6 text-black dark:text-white z-10" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-14 pr-4 py-4 bg-white dark:bg-black border-4 border-black dark:border-white text-black dark:text-white focus:outline-none focus:translate-x-[4px] focus:translate-y-[4px] focus:shadow-none shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] transition-all font-bold placeholder:text-gray-400"
                  placeholder="you@example.com"
                  required
                />
              </div>
            </motion.div>
            
            <motion.div variants={itemVariants} className="space-y-2">
              <div className="flex items-center justify-between">
                <label className="block text-sm font-black text-black dark:text-white uppercase tracking-wider">Password</label>
                <a href="#" className="text-sm font-black text-accent-blue hover:underline uppercase">Forgot?</a>
              </div>
              <div className="relative group">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-6 w-6 text-black dark:text-white z-10" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-14 pr-4 py-4 bg-white dark:bg-black border-4 border-black dark:border-white text-black dark:text-white focus:outline-none focus:translate-x-[4px] focus:translate-y-[4px] focus:shadow-none shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] transition-all font-bold placeholder:text-gray-400"
                  placeholder="••••••••"
                  required
                />
              </div>
            </motion.div>

            <motion.div variants={itemVariants} className="pt-6">
              <button 
                type="submit" 
                disabled={loading} 
                className="w-full bg-black dark:bg-white text-white dark:text-black py-4 px-4 border-4 border-black dark:border-white font-black transition-all flex items-center justify-center gap-2 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] hover:bg-accent-yellow hover:text-black active:translate-x-[4px] active:translate-y-[4px] active:shadow-none dark:active:shadow-none uppercase tracking-widest text-lg"
              >
                {loading ? (
                  <Loader2 className="h-6 w-6 animate-spin" />
                ) : (
                  <>Sign In <ArrowRight className="h-6 w-6" /></>
                )}
              </button>
            </motion.div>

            <motion.div variants={itemVariants}>
              <button
                type="button"
                onClick={handleDemoLogin}
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 border-4 border-black dark:border-white bg-accent-pink px-4 py-4 text-base font-black text-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] hover:bg-white hover:-translate-x-[2px] hover:-translate-y-[2px] hover:shadow-[12px_12px_0px_0px_rgba(0,0,0,1)] active:translate-x-[4px] active:translate-y-[4px] active:shadow-none transition-all duration-200 uppercase tracking-widest mt-4"
              >
                <Layers className="h-6 w-6" />
                Demo Account
              </button>
            </motion.div>
          </form>

          <motion.p variants={itemVariants} className="mt-10 text-center text-sm font-black text-black dark:text-white uppercase tracking-wider">
            New to FinSight?{' '}
            <Link to="/register" className="text-accent-blue border-b-2 border-accent-blue hover:text-black hover:bg-accent-blue hover:border-black transition-all px-1">
              Create an account
            </Link>
          </motion.p>
        </motion.div>
      </div>
    </div>
  );
}
