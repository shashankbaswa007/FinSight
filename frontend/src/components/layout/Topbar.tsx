import { Menu, Moon, Sun, LogOut, User, Bell } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { cn } from '../../utils/cn';

interface TopbarProps {
  onMenuClick: () => void;
}

export default function Topbar({ onMenuClick }: TopbarProps) {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="sticky top-0 z-20 flex h-20 items-center justify-between border-b border-gray-200/50 dark:border-white/5 bg-white/70 dark:bg-ocean-900/50 backdrop-blur-xl px-4 sm:px-6 lg:px-8">
      {/* Left: hamburger */}
      <div className="flex items-center gap-4">
        <button
          onClick={onMenuClick}
          className="lg:hidden p-2.5 rounded-xl bg-white dark:bg-ocean-800 text-gray-500 dark:text-ocean-200 hover:bg-gray-50 dark:hover:bg-ocean-700 shadow-sm border border-gray-200/50 dark:border-white/5 transition-all"
        >
          <Menu className="h-5 w-5" />
        </button>
        <div className="hidden lg:block text-lg font-display font-medium text-gray-900 dark:text-white">
          Overview
        </div>
      </div>

      {/* Right: actions */}
      <div className="flex items-center gap-3">
        {/* Theme toggle */}
        <button
          onClick={toggleTheme}
          className={cn(
            "p-2.5 rounded-xl transition-all duration-300 border shadow-sm",
            "bg-white dark:bg-ocean-800 text-gray-500 dark:text-ocean-200",
            "border-gray-200/50 dark:border-white/5 hover:border-brand-200 dark:hover:border-brand-700",
            "hover:text-brand-600 dark:hover:text-brand-400 hover:bg-brand-50/50 dark:hover:bg-ocean-700"
          )}
          title={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
        </button>

        {/* Notifications (Mock) */}
        <button className={cn(
            "p-2.5 rounded-xl transition-all duration-300 border shadow-sm relative",
            "bg-white dark:bg-ocean-800 text-gray-500 dark:text-ocean-200",
            "border-gray-200/50 dark:border-white/5 hover:border-brand-200 dark:hover:border-brand-700",
            "hover:text-brand-600 dark:hover:text-brand-400 hover:bg-brand-50/50 dark:hover:bg-ocean-700"
          )}>
          <Bell className="h-5 w-5" />
          <span className="absolute top-2.5 right-2.5 flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-brand-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-brand-500"></span>
          </span>
        </button>

        {/* User info */}
        <div className="hidden sm:flex items-center gap-3 pl-2 pr-4 py-1.5 rounded-full bg-white dark:bg-ocean-800 border border-gray-200/50 dark:border-white/5 shadow-sm">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-tr from-brand-500 to-ocean-400 shadow-glow">
            <User className="h-4 w-4 text-white" />
          </div>
          <div className="flex flex-col">
            <span className="text-sm font-semibold text-gray-900 dark:text-white leading-none">
              {user?.name ?? 'User'}
            </span>
            <span className="text-[10px] text-gray-500 dark:text-ocean-300 mt-0.5 leading-none">
              Pro Member
            </span>
          </div>
        </div>

        {/* Logout */}
        <button
          onClick={logout}
          className="ml-1 p-2.5 rounded-xl text-gray-500 hover:bg-red-50 hover:text-red-600 dark:text-ocean-400 dark:hover:bg-red-500/10 dark:hover:text-red-400 transition-colors"
          title="Sign out"
        >
          <LogOut className="h-5 w-5" />
        </button>
      </div>
    </header>
  );
}
