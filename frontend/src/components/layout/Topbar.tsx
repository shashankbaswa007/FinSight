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
    <header className="sticky top-0 z-20 flex h-20 items-center justify-between border-b-2 border-black dark:border-white bg-white dark:bg-black px-4 sm:px-6 lg:px-8">
      {/* Left: hamburger */}
      <div className="flex items-center gap-4">
        <button
          onClick={onMenuClick}
          className="lg:hidden p-2.5 rounded-none bg-accent-yellow text-black border-2 border-black dark:border-white shadow-brutal-sm hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none transition-all"
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
            "p-2.5 rounded-none transition-all duration-200 border-2 shadow-brutal-sm dark:shadow-brutal-dark",
            "bg-accent-blue text-black dark:text-white border-black dark:border-white",
            "hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none dark:hover:shadow-none"
          )}
          title={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
        </button>

        {/* Notifications (Mock) */}
        <button className={cn(
            "p-2.5 rounded-none transition-all duration-200 border-2 shadow-brutal-sm dark:shadow-brutal-dark relative",
            "bg-white dark:bg-black text-black dark:text-white",
            "border-black dark:border-white",
            "hover:bg-accent-yellow hover:text-black hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none dark:hover:shadow-none"
          )}>
          <Bell className="h-5 w-5" />
          <span className="absolute top-2.5 right-2.5 flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-none bg-accent-pink opacity-75"></span>
            <span className="relative inline-flex rounded-none h-2 w-2 bg-accent-red border border-black"></span>
          </span>
        </button>

        {/* User info */}
        <div className="hidden sm:flex items-center gap-3 pl-2 pr-4 py-1.5 rounded-none bg-accent-green border-2 border-black dark:border-white shadow-brutal-sm dark:shadow-brutal-dark">
          <div className="flex h-9 w-9 items-center justify-center rounded-none bg-white border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
            <User className="h-4 w-4 text-black" />
          </div>
          <div className="flex flex-col">
            <span className="text-sm font-bold text-black leading-none">
              {user?.name ?? 'User'}
            </span>
            <span className="text-[10px] text-black/80 font-bold mt-0.5 leading-none">
              Pro Member
            </span>
          </div>
        </div>

        {/* Logout */}
        <button
          onClick={logout}
          className="ml-1 p-2.5 rounded-none border-2 border-black dark:border-white bg-accent-red text-black shadow-brutal-sm dark:shadow-brutal-dark hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none dark:hover:shadow-none transition-all"
          title="Sign out"
        >
          <LogOut className="h-5 w-5" />
        </button>
      </div>
    </header>
  );
}
