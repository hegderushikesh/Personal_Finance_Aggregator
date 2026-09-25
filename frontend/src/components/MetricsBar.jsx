import React, { useEffect, useState } from 'react';
import { motion, useInView } from 'framer-motion';

const metrics = [
  { label: 'Active Wealth Creators', value: 2000000, suffix: '+', format: (val) => `${(val / 1000000).toFixed(0)}M` },
  { label: 'Assets Under Intelligence', value: 12000, suffix: ' Cr+', format: (val) => `₹${val.toLocaleString()}` },
  { label: 'Annual Retention Rate', value: 98, suffix: '%', format: (val) => `${val}` },
  { label: 'Autonomous Monitoring', value: 24, suffix: '/7', format: (val) => `${val}` },
];

function Counter({ metric, inView }) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    if (!inView) return;
    let start = 0;
    const duration = 2000;
    const stepTime = 30;
    const steps = duration / stepTime;
    const increment = metric.value / steps;

    const timer = setInterval(() => {
      start += increment;
      if (start >= metric.value) {
        setCount(metric.value);
        clearInterval(timer);
      } else {
        setCount(Math.floor(start));
      }
    }, stepTime);

    return () => clearInterval(timer);
  }, [inView, metric.value]);

  return (
    <span className="font-display font-extrabold text-3xl sm:text-4xl lg:text-5xl text-[#0F172A]">
      {metric.format(count)}{metric.suffix}
    </span>
  );
}

export default function MetricsBar() {
  const ref = React.useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-50px' });

  return (
    <section ref={ref} className="py-12 bg-[#F3EFEA] border-y border-[#0F172A]/10">
      <div className="w-full max-w-[1920px] mx-auto px-4 sm:px-8 lg:px-12 xl:px-16">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-8 lg:gap-12">
          {metrics.map((metric, idx) => (
            <motion.div
              key={metric.label}
              initial={{ opacity: 0, y: 20 }}
              animate={isInView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.6, delay: idx * 0.1, ease: [0.22, 1, 0.36, 1] }}
              className="flex flex-col items-center text-center group"
            >
              <div className="flex items-center justify-center mb-1 relative">
                <Counter metric={metric} inView={isInView} />
              </div>
              {/* Small Gold Underline Accent */}
              <div className="w-10 h-0.5 bg-[#D4AF37] rounded-full my-2 group-hover:w-16 transition-all duration-300"></div>
              <p className="text-xs sm:text-sm font-semibold uppercase tracking-wider text-[#475569]">
                {metric.label}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
