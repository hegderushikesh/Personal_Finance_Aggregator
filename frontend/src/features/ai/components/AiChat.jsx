import React, { useState, useRef, useEffect } from 'react';
import { Send, Sparkles, Loader2, AlertCircle, Bot } from 'lucide-react';
import AiMessage from './AiMessage';
import QuickQuestions from './QuickQuestions';
import { useAiChat, useAiHealth } from '../hooks/useAi';

export default function AiChat() {
  const [messages, setMessages] = useState([
    {
      id: 1,
      isUser: false,
      message:
        "Hello! I am FinPilot AI, your local financial assistant powered by your private Ollama model. I can analyze your spending, review your budget status, summarize recurring subscriptions, or provide monthly insights. How can I help you today?",
      timestamp: 'Ready',
      model: 'qwen2.5-coder:7b',
    },
  ]);
  const [inputText, setInputText] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const chatMutation = useAiChat();
  const { data: healthData } = useAiHealth();
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, chatMutation.isPending]);

  const handleSendMessage = async (textToSend) => {
    const text = (textToSend || inputText).trim();
    if (!text || chatMutation.isPending) return;

    setErrorMessage('');
    const userMsg = {
      id: Date.now(),
      isUser: true,
      message: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!textToSend) {
      setInputText('');
    }

    try {
      const response = await chatMutation.mutateAsync(text);
      const aiMsg = {
        id: Date.now() + 1,
        isUser: false,
        message: response.message,
        insights: response.insights || [],
        model: response.model || 'qwen2.5-coder:7b',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch (err) {
      const errorMsg =
        err.response?.data?.message ||
        err.userMessage ||
        'Unable to receive AI response. Please verify local Ollama is running.';
      setErrorMessage(errorMsg);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="flex flex-col h-[650px] bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
      {/* Header bar */}
      <div className="p-4 px-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-emerald-100/70 text-emerald-700 flex items-center justify-center">
            <Bot className="w-4 h-4" />
          </div>
          <div>
            <div className="font-semibold text-sm text-slate-800 flex items-center gap-2">
              FinPilot Assistant
              <span className="flex items-center gap-1 text-[11px] font-normal px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                Local {healthData?.model || 'qwen2.5-coder:7b'}
              </span>
            </div>
            <p className="text-[11px] text-slate-400">100% private • On-device financial intelligence</p>
          </div>
        </div>

        <button
          type="button"
          onClick={() =>
            setMessages([
              {
                id: Date.now(),
                isUser: false,
                message: 'Chat cleared. What else would you like to analyze?',
                timestamp: 'Just now',
                model: 'qwen2.5-coder:7b',
              },
            ])
          }
          className="text-xs text-slate-500 hover:text-slate-800 transition-colors font-medium px-2 py-1 rounded cursor-pointer"
        >
          Clear Chat
        </button>
      </div>

      {/* Messages Scroll Area */}
      <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-2">
        {messages.map((m) => (
          <AiMessage
            key={m.id}
            message={m.message}
            isUser={m.isUser}
            timestamp={m.timestamp}
            model={m.model}
            insights={m.insights}
          />
        ))}

        {chatMutation.isPending && (
          <div className="flex items-center gap-3 my-3">
            <div className="w-8 h-8 rounded-full bg-slate-800 text-emerald-400 flex items-center justify-center shrink-0">
              <Bot className="w-4 h-4 animate-spin" />
            </div>
            <div className="bg-slate-50 border border-slate-200 rounded-2xl rounded-tl-xs p-3.5 text-xs text-slate-500 flex items-center gap-2">
              <Loader2 className="w-3.5 h-3.5 animate-spin text-emerald-600" />
              <span>Analyzing financial records with Qwen...</span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Error alert if any */}
      {errorMessage && (
        <div className="mx-4 mb-2 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
          <span className="flex-1">{errorMessage}</span>
          <button
            type="button"
            onClick={() => setErrorMessage('')}
            className="text-rose-500 hover:text-rose-800 font-bold ml-2 cursor-pointer"
          >
            ✕
          </button>
        </div>
      )}

      {/* Quick Prompts & Input Area */}
      <div className="p-4 border-t border-slate-100 bg-white space-y-3">
        <QuickQuestions
          onSelectQuestion={(q) => handleSendMessage(q)}
          disabled={chatMutation.isPending}
        />

        <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl p-1.5 focus-within:border-slate-400 focus-within:ring-2 focus-within:ring-slate-100 transition-all">
          <input
            ref={inputRef}
            type="text"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask about your spending, budget, savings, or subscriptions..."
            disabled={chatMutation.isPending}
            className="flex-1 bg-transparent px-3 py-2 text-sm text-slate-800 placeholder-slate-400 outline-none"
          />
          <button
            type="button"
            onClick={() => handleSendMessage()}
            disabled={!inputText.trim() || chatMutation.isPending}
            className="p-2.5 rounded-lg bg-[#0F172A] hover:bg-slate-800 text-white font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer shrink-0"
          >
            {chatMutation.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin text-emerald-400" />
            ) : (
              <Send className="w-4 h-4" />
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
