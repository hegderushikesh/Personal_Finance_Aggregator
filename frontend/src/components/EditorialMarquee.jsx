import React from 'react';

const marqueeItems = [
  "AUTOMATED SMART SAVINGS",
  "NO HIDDEN FEES",
  "256-BIT ENCRYPTION",
  "RBI COMPLIANT PARTNERS",
  "TAX OPTIMIZED PORTFOLIOS",
  "REAL-TIME SPEND ANALYTICS",
  "INSTANT 0% LIQUIDITY CREDIT",
  "1-TAP VAULT FREEZE",
];

export default function EditorialMarquee() {
  return (
    <section className="py-4 bg-[#0A1128] text-white border-y border-[#D4AF37]/20 overflow-hidden relative selection:bg-[#D4AF37] selection:text-[#0A1128]">
      <div className="flex overflow-hidden whitespace-nowrap group">
        <div className="animate-marquee flex items-center gap-8 py-1">
          {marqueeItems.concat(marqueeItems).map((item, index) => (
            <React.Fragment key={index}>
              <span className="text-xs sm:text-sm font-bold tracking-[0.25em] text-slate-200 uppercase font-display">
                {item}
              </span>
              <span className="inline-block w-2 h-2 rounded-full bg-gold-gradient shadow-sm shadow-[#D4AF37]/50"></span>
            </React.Fragment>
          ))}
        </div>
      </div>
    </section>
  );
}
