import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Sparkles, TrendingUp, ShieldCheck, Zap, Star, CheckCircle2 } from 'lucide-react';

export default function Hero() {
  const [rotateX, setRotateX] = useState(0);
  const [rotateY, setRotateY] = useState(0);

  const handleMouseMove = (e) => {
    const card = e.currentTarget;
    const rect = card.getBoundingClientRect();
    const x = e.clientX - rect.left - rect.width / 2;
    const y = e.clientY - rect.top - rect.height / 2;
    setRotateX(-y * 0.025);
    setRotateY(x * 0.025);
  };

  const handleMouseLeave = () => {
    setRotateX(0);
    setRotateY(0);
  };

  return (
    <section className="w-full relative pt-32 pb-20 md:pt-44 md:pb-32 bg-hero-glow overflow-hidden">
      <div className="w-full  mx-auto px-4 sm:px-6 lg:px-10 xl:px-12">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-10 xl:gap-16 items-center">


          {/* Left Column: Copy & Actions */}
          <div className="lg:col-span-6 flex flex-col items-start text-left">

            {/* Live Badge Pill */}
            <motion.div
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
              className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-[#FAF8F5] border border-[#D4AF37]/30 shadow-sm mb-6"
            >
              <span className="relative flex h-2.5 w-2.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#D4AF37] opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-[#D4AF37]"></span>
              </span>
              <span className="text-xs font-semibold uppercase tracking-wider text-[#0F172A]">
                Smart Vaults 2.0 is live
              </span>
              <Sparkles className="w-3.5 h-3.5 text-[#D4AF37]" />
            </motion.div>

            {/* Masked Headline Reveal */}
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-[#0F172A] leading-[1.1] mb-6">
              <div className="overflow-hidden">
                <motion.span
                  initial={{ y: '110%' }}
                  animate={{ y: 0 }}
                  transition={{ duration: 0.8, delay: 0.1, ease: [0.22, 1, 0.36, 1] }}
                  className="block"
                >
                  Your money.
                </motion.span>
              </div>
              <div className="overflow-hidden">
                <motion.span
                  initial={{ y: '110%' }}
                  animate={{ y: 0 }}
                  transition={{ duration: 0.8, delay: 0.2, ease: [0.22, 1, 0.36, 1] }}
                  className="block text-gold-gradient"
                >
                  Smarter by design.
                </motion.span>
              </div>
            </h1>

            {/* Sub-copy */}
            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.7, delay: 0.35, ease: [0.22, 1, 0.36, 1] }}
              className="text-base sm:text-lg text-[#475569] max-w-xl lg:max-w-2xl mb-8 leading-relaxed font-normal"
            >
            Plan your budget, understand your spending, track your accounts, and get AI-powered financial insights — all in one place.
            </motion.p>


            {/* CTAs */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.7, delay: 0.45, ease: [0.22, 1, 0.36, 1] }}
              className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4 w-full sm:w-auto mb-10"
            >
              <motion.a
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                href="/login"
                className="px-8 py-4 rounded-full bg-gold-gradient text-[#0F172A] font-bold text-base shadow-lg shadow-[#D4AF37]/25 hover:shadow-xl hover:shadow-[#D4AF37]/35 flex items-center justify-center gap-3 transition-all group focus:outline-none focus-visible:ring-2 focus-visible:ring-[#0F172A]"
              >
                <span>Get Started</span>
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </motion.a>
              <motion.a
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                href="#dashboard"
                className="px-8 py-4 rounded-full bg-white/80 border border-[#0F172A]/10 text-[#0F172A] font-semibold text-base shadow-sm hover:bg-white flex items-center justify-center gap-2 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
              >
                <span>Explore Features</span>
              </motion.a>
            </motion.div>

            {/* Trust Social Proof Row */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.7, delay: 0.6 }}
              className="flex items-center gap-4 pt-2 border-t border-[#0F172A]/10 w-full max-w-lg"
            >
              {/* Avatar Stack */}
              <div className="flex -space-x-2">
                <img className="inline-block h-9 w-9 rounded-full ring-2 ring-[#FAF8F5] object-cover" src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80" alt="Investor User" />
                <img className="inline-block h-9 w-9 rounded-full ring-2 ring-[#FAF8F5] object-cover" src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80" alt="Investor User" />
                <img className="inline-block h-9 w-9 rounded-full ring-2 ring-[#FAF8F5] object-cover" src="https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=80" alt="Investor User" />
                <div className="h-9 w-9 rounded-full ring-2 ring-[#FAF8F5] bg-[#0F172A] text-white text-xs font-bold flex items-center justify-center">
                  2M+
                </div>
              </div>

              {/* Rating text */}
              <div className="flex flex-col">
                <div className="flex items-center gap-1">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} className="w-3.5 h-3.5 fill-[#D4AF37] text-[#D4AF37]" />
                  ))}
                  <span className="text-xs font-extrabold text-[#0F172A] ml-1">4.95 / 5</span>
                </div>
                <span className="text-xs text-[#475569]">
                  Trusted by 2,00,000+ Indian investors
                </span>
              </div>
            </motion.div>

          </div>

          {/* Right Column: 3D Mouse Tilt Mockup */}
          <div className="lg:col-span-6 relative flex justify-center items-center">

            {/* Desktop Bobbing Glass Stat Chips (Absolute on Desktop, Flex stack on Mobile) */}
            <div className="relative w-full max-w-lg lg:max-w-xl xl:max-w-2xl">


              {/* Floating Stat Card 1: Invested Growth */}
              <motion.div
                animate={{ y: [0, -10, 0] }}
                transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut' }}
                className="hidden sm:flex absolute -top-6 -left-6 z-20 glass-panel-light p-3.5 rounded-2xl shadow-gold-glow items-center gap-3 border border-[#D4AF37]/30"
              >
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                  <TrendingUp className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-[11px] text-[#475569] font-medium uppercase tracking-wider">Monthly SIP Auto-Grow</div>
                  <div className="text-sm font-extrabold text-[#0F172A] flex items-center gap-1">
                    +₹12,450 <span className="text-xs font-semibold text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded-md">8.4% ↑</span>
                  </div>
                </div>
              </motion.div>

              {/* Floating Stat Card 2: Smart Savings */}
              <motion.div
                animate={{ y: [0, -12, 0] }}
                transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
                className="hidden sm:flex absolute -bottom-6 -right-6 z-20 glass-panel-light p-3.5 rounded-2xl shadow-gold-glow items-center gap-3 border border-[#D4AF37]/30"
              >
                <div className="w-10 h-10 rounded-xl bg-[#D4AF37]/15 text-[#B8860B] flex items-center justify-center font-bold">
                  <Zap className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-[11px] text-[#475569] font-medium uppercase tracking-wider">Tax Saved (Sec 80C)</div>
                  <div className="text-sm font-extrabold text-[#0F172A]">₹46,800 this fiscal</div>
                </div>
              </motion.div>

              {/* Floating Stat Card 3: Security Status */}
              <motion.div
                animate={{ y: [0, -8, 0] }}
                transition={{ duration: 5.5, repeat: Infinity, ease: 'easeInOut', delay: 2 }}
                className="hidden sm:flex absolute top-1/2 -right-8 z-20 glass-panel-light p-3 rounded-xl shadow-md items-center gap-2 border border-[#0F172A]/10"
              >
                <ShieldCheck className="w-4 h-4 text-[#D4AF37]" />
                <span className="text-xs font-semibold text-[#0F172A]">RBI Regulated Vault</span>
              </motion.div>

              {/* Main Dashboard Card with 3D Mouse Tilt */}
              <div
                onMouseMove={handleMouseMove}
                onMouseLeave={handleMouseLeave}
                style={{
                  transform: `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`,
                  transition: 'transform 0.15s ease-out'
                }}
                className="w-full bg-[#0A1128] rounded-[2.5rem] p-6 sm:p-8 shadow-gold-ring text-white border border-[#D4AF37]/30 relative overflow-hidden"
              >
                {/* Gold Radial Glow Inside */}
                <div className="absolute top-0 right-0 w-72 h-72 bg-[#D4AF37]/10 rounded-full filter blur-3xl pointer-events-none"></div>

                {/* Dashboard Header */}
                <div className="flex items-center justify-between pb-6 border-b border-white/10">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-white/10 flex items-center justify-center text-[#D4AF37]">
                      <Sparkles className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-slate-300">Total Net Worth</h3>
                      <p className="text-2xl sm:text-3xl font-extrabold text-white font-display tracking-tight">
                        ₹8,42,316<span className="text-xs text-slate-400 font-normal ml-1">.40</span>
                      </p>
                    </div>
                  </div>
                  <span className="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                    +14.2% YoY
                  </span>
                </div>

                {/* Sparkline Graphic */}
                <div className="my-6">
                  <div className="flex justify-between items-center text-xs text-slate-400 mb-2">
                    <span>Performance (6 Mo)</span>
                    <span className="text-[#E5C158] font-medium">+₹1,04,200</span>
                  </div>
                  <svg className="w-full h-20 overflow-visible" viewBox="0 0 300 60">
                    <defs>
                      <linearGradient id="sparklineGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#D4AF37" stopOpacity="0.4" />
                        <stop offset="100%" stopColor="#D4AF37" stopOpacity="0.0" />
                      </linearGradient>
                    </defs>
                    <path
                      d="M0,45 Q50,15 100,35 T200,10 T300,5"
                      fill="none"
                      stroke="#D4AF37"
                      strokeWidth="3"
                      strokeLinecap="round"
                    />
                    <path
                      d="M0,45 Q50,15 100,35 T200,10 T300,5 L300,60 L0,60 Z"
                      fill="url(#sparklineGrad)"
                    />
                    <circle cx="300" cy="5" r="4" fill="#E5C158" className="animate-ping" />
                    <circle cx="300" cy="5" r="4" fill="#E5C158" />
                  </svg>
                </div>

                {/* Sub-Tiles Grid */}
                <div className="grid grid-cols-2 gap-3 mb-6">
                  <div className="bg-[#1C2541]/80 rounded-2xl p-3.5 border border-white/5">
                    <div className="text-[11px] text-slate-400 mb-1">Smart Vaults</div>
                    <div className="text-base font-bold text-white">₹5,20,000</div>
                    <div className="text-[10px] text-emerald-400 mt-0.5">7.8% Liquid Yield</div>
                  </div>
                  <div className="bg-[#1C2541]/80 rounded-2xl p-3.5 border border-white/5">
                    <div className="text-[11px] text-slate-400 mb-1">Mutual Fund SIP</div>
                    <div className="text-base font-bold text-white">₹3,22,316</div>
                    <div className="text-[10px] text-[#E5C158] mt-0.5">18.4% CAGR</div>
                  </div>
                </div>

                {/* Recent Transaction Rows */}
                <div className="space-y-2.5">
                  <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Live Activity</div>
                  <div className="flex items-center justify-between p-2.5 rounded-xl bg-white/5 text-xs">
                    <div className="flex items-center gap-2.5">
                      <div className="w-7 h-7 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold">
                        ↓
                      </div>
                      <div>
                        <div className="font-semibold text-white">Auto-Roundup Investment</div>
                        <div className="text-[10px] text-slate-400">Nifty 50 Index Fund</div>
                      </div>
                    </div>
                    <span className="font-bold text-emerald-400">+₹450</span>
                  </div>

                  <div className="flex items-center justify-between p-2.5 rounded-xl bg-white/5 text-xs">
                    <div className="flex items-center gap-2.5">
                      <div className="w-7 h-7 rounded-lg bg-[#D4AF37]/20 text-[#E5C158] flex items-center justify-center font-bold">
                        ⚡
                      </div>
                      <div>
                        <div className="font-semibold text-white">Smart Tax Savings</div>
                        <div className="text-[10px] text-slate-400">ELSS Direct Growth</div>
                      </div>
                    </div>
                    <span className="font-bold text-[#E5C158]">₹12,500</span>
                  </div>
                </div>

              </div>

              {/* Mobile Fallback Static Chip Row */}
              <div className="sm:hidden flex flex-col gap-2 mt-4">
                <div className="glass-panel-light p-3 rounded-xl flex items-center justify-between">
                  <span className="text-xs font-semibold text-[#0F172A]">SIP Auto-Grow</span>
                  <span className="text-xs font-bold text-emerald-600">+₹12,450 (8.4%)</span>
                </div>
                <div className="glass-panel-light p-3 rounded-xl flex items-center justify-between">
                  <span className="text-xs font-semibold text-[#0F172A]">Tax Savings</span>
                  <span className="text-xs font-bold text-[#B8860B]">₹46,800 saved</span>
                </div>
              </div>

            </div>

          </div>

        </div>
      </div>
    </section>
  );
}
