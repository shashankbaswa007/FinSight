import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  ArrowLeftRight,
  Wallet,
  BarChart3,
  Tags,
  X,
  TrendingUp,
  Repeat,
  Settings,
  ClipboardCheck,
} from 'lucide-react';
import { cn } from '../../utils/cn';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
  { to: '/reconciliation', label: 'Reconciliation', icon: ClipboardCheck },
  { to: '/budgets', label: 'Budgets', icon: Wallet },
  { to: '/recurring', label: 'Recurring', icon: Repeat },
  { to: '/analytics', label: 'Analytics', icon: BarChart3 },
  { to: '/categories', label: 'Categories', icon: Tags },
  { to: '/settings', label: 'Settings', icon: Settings },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export default function Sidebar({ open, onClose }: SidebarProps) {
  return (
    <aside
      className={cn(
        "fixed inset-y-0 left-0 z-40 w-64 transform transition-all duration-300 ease-in-out lg:relative lg:translate-x-0 lg:z-0",
        "bg-white/80 dark:bg-ocean-900/60 backdrop-blur-xl border-r border-gray-200/50 dark:border-white/5 shadow-[4px_0_24px_rgba(0,0,0,0.02)]",
        open ? "translate-x-0" : "-translate-x-full"
      )}
    >
      {/* Logo */}
      <div className="flex h-20 items-center justify-between px-6 border-b border-gray-200/50 dark:border-white/5">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-tr from-brand-600 to-brand-400 shadow-glow">
            <TrendingUp className="h-5 w-5 text-white" />
          </div>
          <span className="text-xl font-display font-bold text-gray-900 dark:text-white tracking-tight">
            FinSight
          </span>
        </div>
        <button onClick={onClose} className="lg:hidden p-2 rounded-lg bg-gray-100 dark:bg-white/10 text-gray-500 hover:text-gray-900 dark:text-ocean-200 dark:hover:text-white transition-colors">
          <X className="h-5 w-5" />
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
        <div className="text-xs font-semibold text-gray-400 dark:text-ocean-400/60 uppercase tracking-wider mb-4 px-3">Menu</div>
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onClose}
            className={({ isActive }) =>
              cn(
                "group flex items-center gap-3 px-3 py-3 rounded-xl text-sm font-medium transition-all duration-300 relative overflow-hidden",
                isActive
                  ? "text-brand-700 dark:text-brand-300 shadow-sm"
                  : "text-gray-600 dark:text-ocean-200 hover:text-gray-900 dark:hover:text-white hover:bg-white/50 dark:hover:bg-white/5 hover:translate-x-1"
              )
            }
          >
            {({ isActive }) => (
              <>
                {isActive && (
                  <div className="absolute inset-0 bg-brand-50/80 dark:bg-brand-500/10 border border-brand-100 dark:border-brand-500/20 rounded-xl" />
                )}
                <Icon className={cn(
                  "h-5 w-5 flex-shrink-0 transition-transform duration-300 relative z-10",
                  isActive ? "scale-110 text-brand-600 dark:text-brand-400" : "group-hover:scale-110"
                )} />
                <span className="relative z-10">{label}</span>
                {isActive && (
                  <div className="ml-auto h-1.5 w-1.5 rounded-full bg-brand-600 dark:bg-brand-400 relative z-10 shadow-[0_0_8px_rgba(20,184,166,0.8)]" />
                )}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="p-6 border-t border-gray-200/50 dark:border-white/5">
        <div className="flex items-center gap-3 p-3 rounded-xl bg-gray-50/50 dark:bg-black/20 border border-gray-200/50 dark:border-white/5">
           <div className="h-8 w-8 rounded-full bg-gradient-to-br from-brand-400 to-ocean-500 flex items-center justify-center text-white text-xs font-bold shadow-sm">
             FS
           </div>
           <div>
             <p className="text-xs font-medium text-gray-900 dark:text-white">Pro Plan</p>
             <p className="text-[10px] text-gray-500 dark:text-ocean-400">v1.0.0 Alpha</p>
           </div>
        </div>
      </div>
    </aside>
  );
}
