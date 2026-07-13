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
        "bg-white dark:bg-black border-r-2 border-black dark:border-white",
        open ? "translate-x-0" : "-translate-x-full"
      )}
    >
      {/* Logo */}
      <div className="flex h-20 items-center justify-between px-6 border-b-2 border-black dark:border-white">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-none bg-brand-500 border-2 border-black dark:border-white shadow-brutal-sm dark:shadow-brutal-dark">
            <TrendingUp className="h-5 w-5 text-black" />
          </div>
          <span className="text-xl font-display font-bold text-gray-900 dark:text-white tracking-tight">
            FinSight
          </span>
        </div>
        <button onClick={onClose} className="lg:hidden p-2 rounded-none bg-accent-yellow border-2 border-black dark:border-white text-black shadow-brutal-sm hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none transition-all">
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
                "group flex items-center gap-3 px-3 py-3 rounded-none border-2 text-sm font-bold transition-all duration-200 relative overflow-hidden",
                isActive
                  ? "bg-accent-yellow text-black border-black dark:border-white shadow-brutal dark:shadow-brutal-dark"
                  : "bg-white dark:bg-black text-black dark:text-white border-transparent hover:border-black dark:hover:border-white hover:shadow-brutal dark:hover:shadow-brutal-dark"
              )
            }
          >
            {({ isActive }) => (
              <>
                <Icon className={cn(
                  "h-5 w-5 flex-shrink-0 transition-transform duration-200 relative z-10",
                  isActive ? "scale-110" : "group-hover:scale-110"
                )} />
                <span className="relative z-10">{label}</span>
                {isActive && (
                  <div className="ml-auto h-2 w-2 rounded-none bg-black relative z-10" />
                )}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="p-6 border-t-2 border-black dark:border-white">
        <div className="flex items-center gap-3 p-3 rounded-none bg-accent-pink border-2 border-black dark:border-white shadow-brutal-sm dark:shadow-brutal-dark">
           <div className="h-8 w-8 rounded-none bg-white border-2 border-black flex items-center justify-center text-black text-xs font-bold">
             FS
           </div>
           <div>
             <p className="text-xs font-bold text-black">Pro Plan</p>
             <p className="text-[10px] text-black/80 font-bold">v1.0.0 Alpha</p>
           </div>
        </div>
      </div>
    </aside>
  );
}
