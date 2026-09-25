import React from 'react';
import { motion } from 'framer-motion';
import {
  Calculator,
  ChartNoAxesCombined,
  Clock3,
  CreditCard,
  Lightbulb,
  Repeat2,
} from 'lucide-react';

const features = [
  {
    icon: Calculator,
    title: 'Smart budgeting',
    description: 'Give every rupee a purpose and understand exactly how much is available before you spend.',
  },
  {
    icon: CreditCard,
    title: 'Account management',
    description: 'Keep cash, checking, savings, and credit accounts organized in one place.',
  },
  {
    icon: ChartNoAxesCombined,
    title: 'Transaction tracking',
    description: 'Record and categorize your financial activity without losing context.',
  },
  {
    icon: Clock3,
    title: 'Financial analytics',
    description: 'Understand spending patterns, cash flow, income, expenses, and net worth.',
  },
  {
    icon: Repeat2,
    title: 'Recurring payments',
    description: 'Automatically identify recurring expenses based on transaction patterns.',
  },
  {
    icon: Lightbulb,
    title: 'AI financial insights',
    description: 'Ask questions about your finances and receive contextual explanations grounded in your data.',
  },
];

export default function ProductShowcase() {
  return (
    <section id="products" className="bg-[#FAF8F5] py-20 sm:py-28">
      <div className="mx-auto w-full max-w-[1118px] px-6 sm:px-8">
        <div className="mb-12 max-w-[650px] sm:mb-14">
          <div className="mb-4 inline-flex items-center gap-2">
            <span className="h-px w-5 bg-[#D4AF37]" />
            <span className="text-[11px] font-bold uppercase tracking-[0.22em] text-[#B8860B]">
              Features
            </span>
          </div>
          <h2 className="mb-4 max-w-[590px] text-4xl font-extrabold leading-[1.18] tracking-tight text-[#0F172A] sm:text-5xl">
            Everything you need to manage money
          </h2>
          <p className="text-base leading-7 text-[#475569] sm:text-[17px]">
            Six tools that work together instead of six apps that don&apos;t.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-3">
          {features.map(({ icon: Icon, title, description }, index) => (
            <motion.article
              key={title}
              initial={{ opacity: 0, y: 18 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.2 }}
              transition={{ duration: 0.45, delay: index * 0.06, ease: [0.22, 1, 0.36, 1] }}
              whileHover={{ y: -4 }}
              className="min-h-[210px] rounded-[1.5rem] border border-[#0F172A]/10 bg-white p-7 transition-shadow duration-300 hover:shadow-navy-depth"
            >
              <div className="mb-5 flex h-11 w-11 items-center justify-center rounded-xl bg-[#D4AF37]/10 text-[#B8860B]">
                <Icon className="h-5 w-5" strokeWidth={1.8} />
              </div>
              <h3 className="mb-2 text-base font-extrabold text-[#0F172A]">{title}</h3>
              <p className="text-sm leading-[1.55] text-[#475569]">{description}</p>
            </motion.article>
          ))}
        </div>
      </div>
    </section>
  );
}
