import React from 'react';
import { ArrowUpRight, Globe, Share2, MessageCircle, Shield } from 'lucide-react';


const footerLinks = {
  Product: [
    { name: 'Features', href: '#products' },
    { name: 'Budgeting', href: '#products' },
    { name: 'Analytics', href: '#manifesto' },
    { name: 'AI Financial Insights', href: '#products' },
    { name: 'Recurring Payments', href: '#products' },
  ],
  Company: [
    { name: 'About Finly', href: '#security' },
    { name: 'Careers', href: '#' },
    { name: 'Press & Media', href: '#' },
    { name: 'Security Architecture', href: '#security' },
    { name: 'Contact Counselors', href: '#cta' },
  ],
  Resources: [
    { name: 'Documentation', href: '#manifesto' },
    { name: 'GitHub', href: '#dashboard' },
   
  ],
  Legal: [
    { name: 'Privacy Policy', href: '#' },
    { name: 'Terms of Service', href: '#' },
    { name: 'RBI Regulatory Disclosures', href: '#security' },
    { name: 'SEBI Registration Details', href: '#security' },
    { name: 'Grievance Redressal', href: '#' },
  ],
};

export default function Footer() {
  return (
    <footer className="bg-[#070D1F] text-white pt-15 pb-12 border-t border-white/10 selection:bg-[#D4AF37] selection:text-[#070D1F]">
      <div className="w-full max-w-[1920px] mx-auto px-4 sm:px-8 lg:px-10 xl:px-14">
        
        {/* Top Grid: Brand Column + 4 Link Columns */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-12 pb-10 border-b border-white/10">
          
          {/* Brand Column (Span 2) */}
          <div className="lg:col-span-2 space-y-6">
            <a href="#" className="flex items-center gap-3 group focus:outline-none">
              <div className="w-10 h-10 rounded-xl bg-gold-gradient flex items-center justify-center shadow-md">
                <span className="font-display font-extrabold text-xl text-[#070D1F]">F</span>
              </div>
              <div className="flex flex-col">
                <span className="font-display font-extrabold text-2xl tracking-tight text-white leading-none">
                  Finly
                </span>
                <span className="text-[10px] font-bold text-[#E5C158] tracking-[0.18em] uppercase leading-tight">
                  Smarter by design
                </span>
              </div>
            </a>

            <p className="text-sm text-slate-400 max-w-sm leading-relaxed">
              A smarter way to understand your personal finances.
            </p>

            {/* Social Icons */}
            <div className="flex items-center gap-3 pt-2">
              <a
                href="https://twitter.com"
                target="_blank"
                rel="noreferrer"
                aria-label="X / Twitter"
                className="w-9 h-9 rounded-full bg-white/5 hover:bg-[#D4AF37]/20 border border-white/10 hover:border-[#D4AF37]/50 text-slate-300 hover:text-[#E5C158] flex items-center justify-center transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
              >
                <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>
              </a>
              <a
                href="https://linkedin.com"
                target="_blank"
                rel="noreferrer"
                aria-label="LinkedIn"
                className="w-9 h-9 rounded-full bg-white/5 hover:bg-[#D4AF37]/20 border border-white/10 hover:border-[#D4AF37]/50 text-slate-300 hover:text-[#E5C158] flex items-center justify-center transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
              >
                <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24"><path d="M19 3a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h14m-.5 15.5v-5.3a3.26 3.26 0 0 0-3.26-3.26c-.85 0-1.84.52-2.28 1.3v-1.11h-2.79v8.37h2.79v-4.93c0-.77.62-1.4 1.39-1.4a1.4 1.4 0 0 1 1.4 1.4v4.93h2.75M6.46 10.9v8.37H9.25V10.9H6.46M7.86 6.74a1.45 1.45 0 1 0 0 2.9 1.45 1.45 0 0 0 0-2.9z"/></svg>
              </a>
              <a
                href="https://instagram.com"
                target="_blank"
                rel="noreferrer"
                aria-label="Instagram"
                className="w-9 h-9 rounded-full bg-white/5 hover:bg-[#D4AF37]/20 border border-white/10 hover:border-[#D4AF37]/50 text-slate-300 hover:text-[#E5C158] flex items-center justify-center transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
              >
                <svg className="w-4 h-4 stroke-current fill-none stroke-[2]" viewBox="0 0 24 24"><rect width="20" height="20" x="2" y="2" rx="5" ry="5"/><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/><line x1="17.5" x2="17.51" y1="6.5" y2="6.5"/></svg>
              </a>
              <a
                href="https://youtube.com"
                target="_blank"
                rel="noreferrer"
                aria-label="YouTube"
                className="w-9 h-9 rounded-full bg-white/5 hover:bg-[#D4AF37]/20 border border-white/10 hover:border-[#D4AF37]/50 text-slate-300 hover:text-[#E5C158] flex items-center justify-center transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
              >
                <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24"><path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/></svg>
              </a>
            </div>

          </div>

          {/* 4 Link Columns */}
          {Object.entries(footerLinks).map(([category, links]) => (
            <div key={category} className="space-y-4">
              <h4 className="text-xs font-bold text-slate-200 uppercase tracking-widest font-display">
                {category}
              </h4>
              <ul className="space-y-2.5">
                {links.map((link) => (
                  <li key={link.name}>
                    <a
                      href={link.href}
                      className="text-xs text-slate-400 hover:text-[#E5C158] transition-colors inline-flex items-center gap-1 group focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37] rounded"
                    >
                      <span>{link.name}</span>
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}

        </div>

       

        {/* Bottom Bar */}
        <div className="pt-4 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-400">
          <p>© 2026 Finly Technologies Pvt. Ltd. All rights reserved.</p>
          <div className="flex items-center gap-6">
            <span>Crafted with Swiss Precision in India 🇮🇳</span>
          </div>
        </div>

      </div>
    </footer>
  );
}
