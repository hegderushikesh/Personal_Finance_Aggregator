import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { toast } from 'sonner';
import { ArrowRight, CheckCircle2, Sparkles, Shield } from 'lucide-react';

export default function FinalCTA() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!email || !email.includes('@') || !email.includes('.')) {
      toast.error('Please enter a valid email address');
      return;
    }

    setLoading(true);

    setTimeout(() => {
      setLoading(false);
      toast.success('Welcome to Finly Early Access!', {
        description: `We've sent your VIP onboarding link to ${email}`,
        duration: 5000,
      });
      setEmail('');
    }, 800);
  };

  return (
    <section id="cta" className="py-24 bg-[#FAF8F5] relative overflow-hidden bg-cta-radial-glow">
      
      {/* Dashed Concentric-Circle SVG Backdrop */}
      <div className="absolute inset-0 flex items-center justify-center opacity-20 pointer-events-none">
        <svg className="w-[800px] h-[800px]" viewBox="0 0 800 800" fill="none">
          <circle cx="400" cy="400" r="150" stroke="#D4AF37" strokeWidth="1.5" strokeDasharray="6 6" />
          <circle cx="400" cy="400" r="280" stroke="#D4AF37" strokeWidth="1.5" strokeDasharray="8 8" />
          <circle cx="400" cy="400" r="390" stroke="#D4AF37" strokeWidth="1.5" strokeDasharray="10 10" />
        </svg>
      </div>

      <div className="w-full max-w-6xl mx-auto px-4 sm:px-8 lg:px-12 xl:px-16 text-center relative z-10">
        
        {/* Badge Pill */}
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          whileInView={{ opacity: 1, scale: 1 }}
          viewport={{ once: true }}
          className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white border border-[#D4AF37]/40 shadow-sm mb-6"
        >
          <Sparkles className="w-4 h-4 text-[#D4AF37]" />
          <span className="text-xs font-extrabold uppercase tracking-wider text-[#0F172A]">
            Exclusive 2026 Access
          </span>
        </motion.div>

        {/* Headline */}
        <h2 className="text-3xl sm:text-5xl lg:text-6xl font-extrabold text-[#0F172A] tracking-tight mb-6 font-display leading-[1.15]">
          Your financial data <br className="hidden sm:inline" />
          <span className="text-gold-gradient">your control</span>
        </h2>

        {/* Sub-copy */}
        <p className="text-base sm:text-lg text-[#475569] max-w-xl mx-auto mb-10 font-normal">
        Join people who've stopped guessing where their money went.
        </p>

        {/* Working Email Capture Form */}
        <form onSubmit={handleSubmit} className="max-w-md mx-auto mb-8">
          <div className="flex flex-col sm:flex-row items-center gap-3 p-2 bg-white rounded-2xl sm:rounded-full shadow-navy-depth border border-[#0F172A]/10 focus-within:border-[#D4AF37] transition-all">
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your work or personal email"
              aria-label="Email address for early access"
              required
              className="w-full px-5 py-3 text-sm text-[#0F172A] placeholder-slate-400 bg-transparent focus:outline-none rounded-full"
            />
            <button
              type="submit"
              disabled={loading}
              className="w-full sm:w-auto px-7 py-3.5 rounded-full bg-gold-gradient text-[#0F172A] font-bold text-sm shadow-md hover:shadow-lg hover:shadow-[#D4AF37]/30 transition-all flex items-center justify-center gap-2 whitespace-nowrap group focus:outline-none focus-visible:ring-2 focus-visible:ring-[#0F172A]"
            >
              <span>{loading ? 'Securing Spot...' : 'Start Access'}</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </button>
          </div>
        </form>

        {/* Trust Line */}
        <div className="flex flex-wrap items-center justify-center gap-6 text-xs text-[#475569] font-medium">
          {/* <div className="flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            <span>Free forever plan</span>
          </div> */}
          <div className="flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            <span>Free to start · No card required</span>
          </div>
          {/* <div className="flex items-center gap-1.5">
            <Shield className="w-4 h-4 text-[#D4AF37]" />
            <span>256-bit bank encryption</span>
          </div> */}
        </div>

      </div>
    </section>
  );
}
