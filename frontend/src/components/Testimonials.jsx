import React from 'react';
import { motion } from 'framer-motion';
import { Star, Quote } from 'lucide-react';

const testimonials = [
  {
    name: 'Ananya Deshmukh',
    role: 'Product Lead @ Tech unicorn',
    location: 'Bengaluru',
    avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=150&q=80',
    quote: 'Finly transformed how I manage my wealth. The auto-roundups and ELSS tax saving algorithms saved me ₹48,000 in tax within 3 months.',
    monogram: 'AD',
  },
  {
    name: 'Rohan Mehta',
    role: 'VP Engineering',
    location: 'Mumbai',
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80',
    quote: 'The Swiss precision of the UI matched with deep Indian financial context is rare. Having direct credit lines against mutual funds is a game changer.',
    monogram: 'RM',
  },
  {
    name: 'Dr. Kavita Menon',
    role: 'Consultant Cardiologist',
    location: 'Kochi',
    avatar: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&w=150&q=80',
    quote: 'As a busy medical professional, I never had time for portfolio rebalancing. Finly automates everything with absolute security and zero hidden fees.',
    monogram: 'KM',
  },
  {
    name: 'Siddharth Nair',
    role: 'Founder & CEO',
    location: 'Hyderabad',
    avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=150&q=80',
    quote: 'The level of craftsmanship in this app is unmatched. From live Account Aggregator feeds to instant credit disbursal, it’s flawless.',
    monogram: 'SN',
  },
  {
    name: 'Pooja Agarwal',
    role: 'Senior Financial Analyst',
    location: 'Delhi NCR',
    avatar: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=150&q=80',
    quote: 'Finly’s net worth command center gives me complete visibility across stocks, mutual funds, EPF, and sovereign gold bonds.',
    monogram: 'PA',
  },
  {
    name: 'Aditya Sen',
    role: 'Design Director',
    location: 'Pune',
    avatar: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=150&q=80',
    quote: 'Finally a fintech application that respects design aesthetics without compromising on robust security and regulatory compliance.',
    monogram: 'AS',
  },
];

export default function Testimonials() {
  return (
    <section className="py-24 bg-[#F3EFEA] border-y border-[#0F172A]/10">
      <div className="w-full max-w-[1920px] mx-auto px-4 sm:px-8 lg:px-12 xl:px-16">
        
        {/* Section Title */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-2 mb-3">
            <span className="w-8 h-[2px] bg-[#D4AF37]"></span>
            <span className="text-xs uppercase tracking-[0.22em] font-bold text-[#D4AF37]">
              Community Voices
            </span>
            <span className="w-8 h-[2px] bg-[#D4AF37]"></span>
          </div>
          <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0F172A] tracking-tight font-display">
            Loved by India's top <span className="text-gold-gradient">investors & leaders.</span>
          </h2>
        </div>

        {/* Responsive Grid / Mobile Snap Carousel */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 snap-x snap-mandatory overflow-x-auto pb-4 md:overflow-visible">
          {testimonials.map((t, idx) => (
            <motion.div
              key={t.name}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: idx * 0.08 }}
              whileHover={{ y: -6 }}
              className="snap-center min-w-[280px] bg-white rounded-[1.75rem] p-7 shadow-navy-depth border border-[#0F172A]/10 hover:border-[#D4AF37]/50 transition-all duration-300 flex flex-col justify-between"
            >
              <div>
                {/* Top Row: Stars + Quote Icon */}
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-1">
                    {[...Array(5)].map((_, i) => (
                      <Star key={i} className="w-4 h-4 fill-[#D4AF37] text-[#D4AF37]" />
                    ))}
                  </div>
                  <Quote className="w-6 h-6 text-[#D4AF37]/40" />
                </div>

                <p className="text-sm text-[#475569] leading-relaxed mb-6 font-normal italic">
                  "{t.quote}"
                </p>
              </div>

              {/* User Bio */}
              <div className="flex items-center gap-3 pt-4 border-t border-[#0F172A]/5">
                <div className="relative">
                  <img
                    src={t.avatar}
                    alt={t.name}
                    className="w-11 h-11 rounded-full object-cover ring-2 ring-[#D4AF37]/30"
                  />
                  <div className="absolute -bottom-1 -right-1 w-5 h-5 rounded-full bg-gold-gradient flex items-center justify-center text-[9px] font-extrabold text-[#0F172A]">
                    {t.monogram}
                  </div>
                </div>
                <div>
                  <h4 className="text-sm font-extrabold text-[#0F172A] leading-tight font-display">
                    {t.name}
                  </h4>
                  <p className="text-xs text-[#475569]">
                    {t.role} · <span className="text-[#B8860B] font-medium">{t.location}</span>
                  </p>
                </div>
              </div>
            </motion.div>
          ))}
        </div>

      </div>
    </section>
  );
}
