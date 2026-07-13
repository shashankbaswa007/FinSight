/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6', // Vivid Blue
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
          950: '#172554',
        },
        ocean: {
          50: '#ffffff',
          100: '#f4f4f5',
          200: '#e4e4e7',
          300: '#d4d4d8',
          400: '#a1a1aa',
          500: '#71717a',
          600: '#52525b',
          700: '#3f3f46',
          800: '#27272a',
          900: '#18181b',
          950: '#000000',
        },
        accent: {
          yellow: '#facc15',
          pink: '#f472b6',
          green: '#4ade80',
          red: '#f87171'
        }
      },
      fontFamily: {
        sans: ['"Space Grotesk"', '"Inter"', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['"Space Grotesk"', '"Inter"', 'system-ui', '-apple-system', 'sans-serif'],
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-out both',
        'slide-up': 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) both',
        'slide-down': 'slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1) both',
        'scale-in': 'scaleIn 0.2s cubic-bezier(0.16, 1, 0.3, 1) both',
        'stagger-1': 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) 0.05s both',
        'stagger-2': 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) 0.10s both',
        'stagger-3': 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) 0.15s both',
        'stagger-4': 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) 0.20s both',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideDown: {
          '0%': { opacity: '0', transform: 'translateY(-10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        }
      },
      boxShadow: {
        'brutal': '4px 4px 0px 0px rgba(0,0,0,1)',
        'brutal-lg': '8px 8px 0px 0px rgba(0,0,0,1)',
        'brutal-sm': '2px 2px 0px 0px rgba(0,0,0,1)',
        'brutal-dark': '4px 4px 0px 0px rgba(255,255,255,1)',
        'brutal-lg-dark': '8px 8px 0px 0px rgba(255,255,255,1)',
      }
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
};
