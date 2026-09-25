import React from 'react';
import { motion } from 'framer-motion';
import { ShieldCheck, Lock, EyeOff, CheckCircle2, Award, Zap, Building } from 'lucide-react';

const pillars = [
  {
    icon: ShieldCheck,
    title: 'Bank-Grade Security',
    desc: 'Hardware Security Modules (HSM) and 256-bit AES encryption protect all credentials, transaction data, and linked accounts.',
  },
  {
    icon: Lock,
    title: 'End-to-End Data Encryption',
    desc: 'Zero plain-text storage. Your sensitive financial data is encrypted in transit and at rest with strict key rotation policies.',
  },
  {
    icon: EyeOff,
    title: 'Privacy-First Architecture',
    desc: 'We never sell your data or serve third-party ads. Your portfolio metadata is strictly isolated to your private vault.',
  },
];

const trustBadges = [
  { name: 'RBI Regulated Partners', icon: Building },
  { name: 'ISO 27001 Certified', icon: Award },
  { name: 'NPCI UPI Integrated', icon: Zap },
  { name: 'PCI DSS Level 1', icon: Lock },
  { name: '1-Tap Vault Freeze', icon: ShieldCheck },
];

export default function SecuritySection() {
  return (
    <section id="security" className="py-24 bg-[#0B132B] text-white relative overflow-hidden">
      
      {/* Background Subtle Gold Glow */}
      <div className="absolute bottom-0 right-0 w-96 h-96 bg-[#D4AF37]/10 rounded-full filter blur-[100px] pointer-events-none"></div>

      <div className="w-full max-w-[1920px] mx-auto px-4 sm:px-8 lg:px-12 xl:px-16 relative z-10">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-2 mb-3">
            <span className="w-8 h-[2px] bg-[#D4AF37]"></span>
            <span className="text-xs uppercase tracking-[0.22em] font-bold text-[#E5C158]">
              Uncompromising Protection
            </span>
            <span className="w-8 h-[2px] bg-[#D4AF37]"></span>
          </div>
          <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-white tracking-tight font-display mb-4">
            Fortress-level security for <span className="text-gold-gradient">your hard-earned capital.</span>
          </h2>
          <p className="text-base sm:text-lg text-slate-300">
            Engineered to comply with the highest standards of the Reserve Bank of India and SEBI regulatory frameworks.
          </p>
        </div>

        {/* 3 Pillars Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-16">
          {pillars.map((p, idx) => {
            const Icon = p.icon;
            return (
              <motion.div
                key={p.title}
                initial={{ opacity: 0, y: 25 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.6, delay: idx * 0.1 }}
                className="bg-[#0A1128] rounded-[2rem] p-8 border border-[#D4AF37]/20 hover:border-[#D4AF37]/50 shadow-gold-ring transition-all duration-300 flex flex-col justify-between"
              >
                <div>
                  <div className="w-14 h-14 rounded-2xl bg-gold-gradient text-[#0A1128] flex items-center justify-center font-bold mb-6 shadow-md shadow-[#D4AF37]/20">
                    <Icon className="w-7 h-7" />
                  </div>
                  <h3 className="text-xl font-extrabold text-white mb-3 font-display">
                    {p.title}
                  </h3>
                  <p className="text-sm text-slate-300 leading-relaxed">
                    {p.desc}
                  </p>
                </div>
              </motion.div>
            );
          })}
        </div>

        {/* Trust Badge Pill Row */}
        <div className="pt-8 border-t border-white/10">
          <div className="text-center text-xs font-semibold text-slate-400 uppercase tracking-widest mb-6">
            Institutional Trust & Compliance Ecosystem
          </div>
          <div className="flex flex-wrap items-center justify-center gap-3 sm:gap-4">
            {trustBadges.map((badge) => {
              const BadgeIcon = badge.icon;
              return (
                <div
                  key={badge.name}
                  className="px-4 py-2 rounded-full bg-[#1C2541] border border-white/10 text-slate-200 text-xs font-bold flex items-center gap-2 hover:border-[#D4AF37]/40 transition-colors"
                >
                  <BadgeIcon className="w-3.5 h-3.5 text-[#E5C158]" />
                  <span>{badge.name}</span>
                </div>
              );
            })}
          </div>
        </div>

      </div>
    </section>
  );
}
