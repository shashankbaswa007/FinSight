import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { BrainCircuit, Sparkles, X, Send } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import api from '../api/axios';

interface Message {
  id: string;
  sender: 'user' | 'ai';
  text: string;
}

const AiChatWidget: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    { id: '1', sender: 'ai', text: 'Hi! I am your AI Financial Advisor. How can I help you today?' }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const toggleChat = () => setIsOpen(!isOpen);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputValue.trim() || isLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      sender: 'user',
      text: inputValue.trim(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      const aiMessageId = (Date.now() + 1).toString();
      let aiResponseText = "";
      
      setMessages((prev) => [...prev, { id: aiMessageId, sender: 'ai', text: '' }]);
      setIsLoading(false); // Stop loading spinner since we are streaming now

      const token = localStorage.getItem('token');
      
      await fetchEventSource('/api/v1/ai/chat/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ message: inputValue.trim() }),
        onmessage(ev) {
          try {
            const data = JSON.parse(ev.data);
            if (data.text) {
              aiResponseText += data.text;
              setMessages((prev) => 
                prev.map(m => m.id === aiMessageId ? { ...m, text: aiResponseText } : m)
              );
            }
          } catch (e) {
            console.error("Failed to parse SSE JSON:", e, ev.data);
          }
        },
        onerror(err) {
          console.error("SSE Error:", err);
          throw err;
        }
      });
      
    } catch (error) {
      console.error('Failed to get AI response:', error);
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'ai',
        text: 'Sorry, I am having trouble connecting right now. Please try again later.',
      };
      setMessages((prev) => [...prev, errorMessage]);
    }
  };

  return (
    <div className="fixed bottom-6 right-6 z-50">
      {/* Chat Window */}
      <AnimatePresence>
      {isOpen && (
        <motion.div 
          initial={{ opacity: 0, y: 20, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: 20, scale: 0.95 }}
          transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
          className="mb-4 w-[340px] sm:w-[400px] h-[550px] max-h-[80vh] glass-panel flex flex-col overflow-hidden origin-bottom-right shadow-2xl shadow-brand-500/10"
        >
          {/* Header */}
          <div className="bg-gradient-to-r from-brand-600 to-ocean-500 p-5 text-white flex justify-between items-center shadow-md z-10 relative overflow-hidden">
            <div className="absolute inset-0 bg-glass-gradient opacity-50"></div>
            <div className="flex items-center space-x-3 relative z-10">
              <div className="w-10 h-10 bg-white/20 backdrop-blur-md rounded-xl flex items-center justify-center border border-white/30 shadow-inner">
                <BrainCircuit className="h-6 w-6 text-white" />
              </div>
              <div>
                <h3 className="font-display font-semibold text-base tracking-tight">FinSight AI</h3>
                <p className="text-xs text-brand-100 font-medium">Pro Financial Advisor</p>
              </div>
            </div>
            <button 
              onClick={toggleChat}
              className="text-white/80 hover:text-white transition-all p-2 rounded-xl hover:bg-white/20 relative z-10"
              aria-label="Close chat"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-5 space-y-5 bg-white/40 dark:bg-ocean-950/40 scroll-smooth backdrop-blur-md">
            {messages.map((msg) => (
              <motion.div
                key={msg.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm shadow-sm ${
                    msg.sender === 'user'
                      ? 'bg-gradient-to-br from-brand-500 to-brand-600 text-white rounded-br-sm shadow-brand-500/20'
                      : 'bg-white dark:bg-ocean-800 text-gray-800 dark:text-gray-100 border border-gray-100 dark:border-ocean-700 rounded-bl-sm shadow-sm prose prose-sm prose-brand dark:prose-invert max-w-none prose-p:leading-relaxed prose-li:my-0.5 prose-ul:my-2 prose-strong:text-brand-600 dark:prose-strong:text-brand-400'
                  }`}
                >
                  {msg.sender === 'user' ? (
                    <p className="whitespace-pre-wrap leading-relaxed m-0">{msg.text}</p>
                  ) : (
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>{msg.text}</ReactMarkdown>
                  )}
                </div>
              </motion.div>
            ))}
            {isLoading && (
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex justify-start">
                <div className="bg-white dark:bg-ocean-800 border border-gray-100 dark:border-ocean-700 rounded-2xl rounded-bl-sm px-4 py-3 shadow-sm flex space-x-1.5 items-center">
                  <div className="w-1.5 h-1.5 bg-brand-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
                  <div className="w-1.5 h-1.5 bg-brand-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
                  <div className="w-1.5 h-1.5 bg-brand-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
                </div>
              </motion.div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Area */}
          <div className="p-4 bg-white/80 dark:bg-ocean-900/80 backdrop-blur-xl border-t border-gray-100 dark:border-ocean-800 relative z-10">
            <form onSubmit={handleSend} className="relative flex items-center">
              <input
                type="text"
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                placeholder="Ask your AI advisor..."
                className="w-full pl-5 pr-14 py-3.5 bg-gray-50/50 dark:bg-ocean-950/50 border border-gray-200/50 dark:border-ocean-700 rounded-2xl focus:outline-none focus:ring-2 focus:ring-brand-500/50 dark:text-white text-sm transition-all hover:bg-white dark:hover:bg-ocean-900"
                disabled={isLoading}
              />
              <button
                type="submit"
                disabled={!inputValue.trim() || isLoading}
                className="absolute right-2 w-10 h-10 flex items-center justify-center bg-gradient-to-r from-brand-600 to-brand-500 hover:from-brand-500 hover:to-brand-400 text-white rounded-xl disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-glow"
                aria-label="Send message"
              >
                <Send className="w-4 h-4 translate-x-px" />
              </button>
            </form>
          </div>
        </motion.div>
      )}
      </AnimatePresence>

      {/* Floating Toggle Button */}
      <AnimatePresence>
      {!isOpen && (
        <motion.button
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.8 }}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={toggleChat}
          className="w-16 h-16 bg-gradient-to-br from-brand-500 via-brand-600 to-ocean-600 text-white rounded-2xl shadow-glow flex items-center justify-center relative group border border-white/20"
          aria-label="Open AI Advisor"
        >
          <BrainCircuit className="h-8 w-8 text-white group-hover:scale-110 transition-transform duration-300" />
          <span className="absolute -top-1.5 -right-1.5 flex h-4 w-4">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-brand-300 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-4 w-4 bg-brand-400 border-[2px] border-white dark:border-ocean-950"></span>
          </span>
        </motion.button>
      )}
      </AnimatePresence>
    </div>
  );
};

export default AiChatWidget;
