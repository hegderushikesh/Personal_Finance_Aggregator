import { useEffect } from 'react';
import Lenis from 'lenis';
import Navbar from '../components/Navbar';
import Hero from '../components/Hero';
import MetricsBar from '../components/MetricsBar';
import EditorialMarquee from '../components/EditorialMarquee';
import ProductShowcase from '../components/ProductShowcase';
import InteractiveDashboard from '../components/InteractiveDashboard';
import Manifesto from '../components/Manifesto';
import Testimonials from '../components/Testimonials';
import SecuritySection from '../components/SecuritySection';
import FinalCTA from '../components/FinalCTA';
import Footer from '../components/Footer';

export default function LandingPage() {
  useEffect(() => {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReducedMotion) return;

    const lenis = new Lenis({
      duration: 1.2,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -2 * t)),
      orientation: 'vertical',
      gestureOrientation: 'vertical',
      smoothWheel: true,
      wheelMultiplier: 1,
      touchMultiplier: 2,
    });

    function raf(time) {
      lenis.raf(time);
      requestAnimationFrame(raf);
    }

    requestAnimationFrame(raf);

    return () => {
      lenis.destroy();
    };
  }, []);

  return (
    <div className="bg-[#FAF8F5] text-[#0F172A] selection:bg-[#D4AF37]/20 selection:text-[#0F172A] font-sans antialiased overflow-x-hidden">
      <Navbar />
      <main>
        <Hero />
        {/* <MetricsBar /> */}
        <EditorialMarquee />
        <ProductShowcase />
        <InteractiveDashboard />
        <Manifesto />
        <Testimonials />
        <SecuritySection />
        <FinalCTA />
      </main>
      <Footer />
    </div>
  );
}
