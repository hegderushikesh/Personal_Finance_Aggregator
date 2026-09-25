import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Menu, X, ArrowUpRight } from 'lucide-react';

const navLinks = [
  { name: 'Features', href: '#features' },
  { name: 'How it Works', href: '#dashboard' },
  { name: 'AI', href: '#manifesto' },
  { name: 'Security', href: '#security' },
];

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 20) {
        setScrolled(true);
      } else {
        setScrolled(false);
      }
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled
          ? 'bg-[#FAF8F5]/85 backdrop-blur-md border-b border-[#0F172A]/10 shadow-sm py-3.5'
          : 'bg-transparent py-5'
      }`}
    >
      <div className="w-full max-w-[1920px] mx-auto px-4 sm:px-8 lg:px-12 xl:px-16 flex items-center justify-between">

        {/* Logo */}
        <a href="/" className="flex items-center gap-3 group focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37] rounded-full p-1">
          <div className="w-10 h-10 rounded-xl bg-gold-gradient flex items-center justify-center shadow-sm group-hover:scale-105 transition-transform duration-300">
            <span className="font-display font-black text-xl text-[#0F172A]">F</span>
          </div>
          <div className="flex flex-col">
            <span className="font-display font-extrabold text-2xl tracking-tight text-[#0F172A] leading-none">
              FinPilot
            </span>
            <span className="text-[10px] font-semibold text-[#D4AF37] tracking-[0.18em] uppercase leading-tight">
              Smarter by design
            </span>
          </div>
        </a>

        {/* Desktop Nav Links */}
        <nav className="hidden md:flex items-center gap-8">
          {navLinks.map((link) => (
            <a
              key={link.name}
              href={link.href}
              className="text-sm font-medium text-[#475569] hover:text-[#0F172A] transition-colors relative py-1 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37] rounded"
            >
              {link.name}
            </a>
          ))}
        </nav>

        {/* Right CTA Actions */}
        <div className="hidden md:flex items-center gap-4">
          <Link
            to="/login"
            className="px-5 py-2.5 text-sm font-semibold text-[#0F172A] hover:text-[#D4AF37] transition-colors rounded-full focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
          >
            Log in
          </Link>
          <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.98 }}>
            <Link
              to="/login"
              className="px-6 py-2.5 text-sm font-semibold text-white bg-[#0F172A] hover:bg-gold-gradient hover:text-[#0F172A] transition-all duration-300 rounded-full shadow-sm flex items-center gap-2 group focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
            >
              <span>Get Started</span>
              <ArrowUpRight className="w-4 h-4 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform" />
            </Link>
          </motion.div>
        </div>

        {/* Mobile Hamburger Button */}
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle Navigation Menu"
          aria-expanded={mobileMenuOpen}
          className="md:hidden min-w-[44px] min-h-[44px] flex items-center justify-center p-2 rounded-full text-[#0F172A] hover:bg-[#F3EFEA] focus:outline-none focus-visible:ring-2 focus-visible:ring-[#D4AF37]"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Mobile Drawer Menu */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
            className="md:hidden bg-[#FAF8F5]/98 backdrop-blur-xl border-b border-[#0F172A]/10 px-6 py-6 overflow-hidden"
          >
            <div className="flex flex-col gap-4">
              {navLinks.map((link, idx) => (
                <motion.a
                  key={link.name}
                  initial={{ opacity: 0, x: -16 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: idx * 0.05 + 0.1 }}
                  href={link.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className="min-h-[44px] flex items-center text-lg font-semibold text-[#0F172A] hover:text-[#D4AF37] border-b border-[#0F172A]/5 py-2"
                >
                  {link.name}
                </motion.a>
              ))}
              <div className="flex flex-col gap-3 pt-4">
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="min-h-[44px] w-full flex items-center justify-center rounded-full text-[#0F172A] font-semibold border border-[#0F172A]/20"
                >
                  Log in
                </Link>
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="min-h-[44px] w-full flex items-center justify-center rounded-full bg-gold-gradient text-[#0F172A] font-bold shadow-md"
                >
                  Get Started Free
                </Link>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
}
