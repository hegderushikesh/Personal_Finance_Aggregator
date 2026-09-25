import React from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Bot, Building2, Calculator, CreditCard, Check, Sparkles } from 'lucide-react';

const manifestoChapters = [
  {
    num: '01',
    eyebrow: 'Unified Open Banking',
    title: 'Connect.',
    desc: 'Connect your financial accounts or add them manually.',
    cta: 'Explore Account Aggregator',
    visual: 'bank-list',
  },
  {
    num: '02',
    eyebrow: 'Autonomous AI Wealth',
    title: 'Organize.',
    desc: 'Track accounts, transactions, categories, and recurring payments.',
    cta: 'Discover AI Wealth Engine',
    visual: 'ai-chat',
  },
  {
    num: '03',
    eyebrow: 'Tax Optimization Engine',
    title: 'Plan.',
    desc: 'Create budgets and financial targets. Receive actionable tax-saving recommendations.',
    cta: 'Calculate Tax Savings',
    visual: 'tax-card',
  },
  {
    num: '04',
    eyebrow: 'Prestige Membership',
    title: 'Understand.',
    desc: 'Crafted from anodized black titanium with physical gold inlay. Unlocks zero-markup forex rates, airport lounge access, and 3% cashback on global spends.Use analytics and AI-powered insights to understand your finances.',
    cta: 'Apply for Metal Card',
    visual: 'metal-card',
  },
];

export default function Manifesto() {
  return (
    <section id="manifesto" className="py-24 bg-[#FAF8F5]">
      <div className="w-full max-w-[1920px] mx-auto px-4 sm:px-8 lg:px-12 xl:px-16 space-y-24">
        
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 mb-3">
            <span className="w-8 h-[2px] bg-[#D4AF37]"></span>
            <span className="text-xs uppercase tracking-[0.22em] font-bold text-[#D4AF37]">
              Manifesto & Principles
            </span>
            <span className="w-8 h-[2px] bg-[#D4AF37]"></span>
          </div>
          <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0F172A] tracking-tight font-display">
            The four pillars of <span className="text-gold-gradient">hyper-intelligent wealth.</span>
          </h2>
        </div>

        {/* Alternating Chapters */}
        {manifestoChapters.map((chapter, idx) => {
          const isEven = idx % 2 === 1;

          return (
            <motion.div
              key={chapter.num}
              initial={{ opacity: 0, y: 35 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
              className={`grid grid-cols-1 lg:grid-cols-12 gap-12 items-center ${
                isEven ? 'lg:flex-row-reverse' : ''
              }`}
            >
              {/* Text Side */}
              <div className={`lg:col-span-6 ${isEven ? 'lg:order-2' : 'lg:order-1'}`}>
                {/* Outlined Ghost Number */}
                <div className="text-6xl sm:text-7xl font-black font-display text-transparent stroke-gold tracking-tight mb-2 select-none" style={{ WebkitTextStroke: '2px #D4AF37', opacity: 0.6 }}>
                  {chapter.num}
                </div>
                
                <span className="text-xs uppercase tracking-[0.2em] font-bold text-[#D4AF37] mb-2 block">
                  — {chapter.eyebrow}
                </span>
                
                <h3 className="text-2xl sm:text-3xl lg:text-4xl font-extrabold text-[#0F172A] mb-4 font-display leading-tight">
                  {chapter.title}
                </h3>
                
                <p className="text-base text-[#475569] mb-6 leading-relaxed">
                  {chapter.desc}
                </p>

                <a
                  href="#cta"
                  className="inline-flex items-center gap-2 text-sm font-bold text-[#0F172A] hover:text-[#B8860B] transition-colors group focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37] rounded"
                >
                  <span className="border-b-2 border-[#D4AF37] pb-0.5">{chapter.cta}</span>
                  <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform text-[#D4AF37]" />
                </a>
              </div>

              {/* Bespoke Visual Graphic Side */}
              <div className={`lg:col-span-6 ${isEven ? 'lg:order-1' : 'lg:order-2'}`}>
                
                {/* Visual 1: Linked Banks */}
                {chapter.visual === 'bank-list' && (
                  <div className="bg-white rounded-[2rem] p-6 shadow-navy-depth border border-[#0F172A]/10 space-y-3">
                    <div className="text-xs font-bold text-[#475569] uppercase tracking-wider mb-2">Live Bank Aggregator Feeds</div>
                    {[
                      { name: 'HDFC Bank Direct Account', balance: '₹4,82,100', status: 'Syncing live', logo: '🏦' },
                      { name: 'ICICI Wealth Savings', balance: '₹2,15,400', status: 'Verified AA', logo: '🏛️' },
                      { name: 'State Bank of India', balance: '₹1,44,816', status: 'Encrypted', logo: '⚡' },
                      { name: 'Axis Bank Salary Account', balance: '₹95,200', status: 'Connected', logo: '💳' },
                    ].map((b, i) => (
                      <div key={i} className="flex items-center justify-between p-3 rounded-xl bg-[#FAF8F5] border border-[#0F172A]/5">
                        <div className="flex items-center gap-3">
                          <span className="text-xl">{b.logo}</span>
                          <div>
                            <div className="text-xs font-extrabold text-[#0F172A]">{b.name}</div>
                            <div className="text-[10px] text-emerald-600 font-semibold">{b.status}</div>
                          </div>
                        </div>
                        <span className="text-sm font-bold text-[#0F172A]">{b.balance}</span>
                      </div>
                    ))}
                  </div>
                )}

                {/* Visual 2: AI Chat Mock */}
                {chapter.visual === 'ai-chat' && (
                  <div className="bg-[#0A1128] rounded-[2rem] p-6 text-white shadow-gold-ring border border-[#D4AF37]/30 space-y-4">
                    <div className="flex items-center gap-2 pb-3 border-b border-white/10">
                      <div className="w-8 h-8 rounded-full bg-gold-gradient flex items-center justify-center text-[#0A1128] font-black text-xs">
                        AI
                      </div>
                      <div>
                        <div className="text-xs font-bold text-white">Finly Wealth Assistant</div>
                        <div className="text-[10px] text-emerald-400">Online · Autonomous Mode</div>
                      </div>
                    </div>
                    <div className="space-y-3 text-xs">
                      <div className="bg-white/10 p-3 rounded-2xl max-w-[85%] text-slate-200">
                        "Your ₹45,000 monthly SIP can be re-routed to 0%-commission Direct Index funds saving ₹18,200 in TER expenses."
                      </div>
                      <div className="bg-gold-gradient text-[#0A1128] p-3 rounded-2xl max-w-[85%] ml-auto font-semibold shadow-sm">
                        "Apply rebalancing now."
                      </div>
                      <div className="bg-white/10 p-3 rounded-2xl max-w-[85%] text-emerald-400 font-semibold flex items-center gap-2">
                        <Check className="w-4 h-4" />
                        "Done! Portfolios updated seamlessly."
                      </div>
                    </div>
                  </div>
                )}

                {/* Visual 3: Tax Savings Card */}
                {chapter.visual === 'tax-card' && (
                  <div className="bg-white rounded-[2rem] p-6 shadow-navy-depth border border-[#0F172A]/10 space-y-4">
                    <div className="flex justify-between items-center">
                      <span className="text-xs font-bold uppercase tracking-wider text-[#B8860B]">Section 80C & 80D Optimizer</span>
                      <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
                        Maximized
                      </span>
                    </div>
                    <div className="bg-[#FAF8F5] p-4 rounded-xl space-y-2 border border-[#0F172A]/5">
                      <div className="flex justify-between text-xs font-semibold text-[#475569]">
                        <span>ELSS Mutual Fund SIP</span>
                        <span className="text-[#0F172A]">₹1,50,000 / ₹1,50,000</span>
                      </div>
                      <div className="w-full h-2 bg-slate-200 rounded-full">
                        <div className="w-full h-full bg-gold-gradient rounded-full"></div>
                      </div>
                      <div className="text-right text-[11px] text-emerald-600 font-bold">Tax Saved: ₹46,800</div>
                    </div>
                  </div>
                )}

                {/* Visual 4: Metal Signature Card */}
                {chapter.visual === 'metal-card' && (
                  <div className="relative group">
                    <div className="w-full h-56 rounded-[2rem] bg-gradient-to-tr from-[#0A1128] via-[#1C2541] to-[#0A1128] p-6 text-white border border-[#D4AF37]/50 shadow-gold-ring flex flex-col justify-between overflow-hidden relative">
                      <div className="absolute top-0 right-0 w-40 h-40 bg-gold-gradient opacity-10 rounded-full blur-2xl"></div>
                      <div className="flex justify-between items-center z-10">
                        <span className="font-display font-extrabold text-xl tracking-tight text-gold-gradient">Finly</span>
                        <span className="text-xs font-bold text-[#E5C158] tracking-widest uppercase">PRESTIGE METAL</span>
                      </div>
                      <div className="z-10 space-y-1">
                        <div className="text-sm font-mono tracking-widest text-slate-300">•••• •••• •••• 8842</div>
                        <div className="flex justify-between text-[11px] text-slate-400 font-medium">
                          <span>VIKRAM SHARMA</span>
                          <span>EXP 08/30</span>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

              </div>
            </motion.div>
          );
        })}

      </div>
    </section>
  );
}
