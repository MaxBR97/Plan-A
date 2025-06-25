import React, { useRef, useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import './InfoIcon.css';

const InfoIcon = ({ tooltip }) => {
  const iconRef = useRef(null);
  const tooltipRef = useRef(null);
  const [tooltipPosition, setTooltipPosition] = useState({ top: 'auto', left: 'auto' });
  const [showTooltip, setShowTooltip] = useState(false);

  const updateTooltipPosition = () => {
    if (!iconRef.current || !tooltipRef.current) return;

    const iconRect = iconRef.current.getBoundingClientRect();
    const tooltipRect = tooltipRef.current.getBoundingClientRect();
    const viewportHeight = window.innerHeight;
    const viewportWidth = window.innerWidth;

    // Calculate initial position (centered above the icon)
    let top = iconRect.top - tooltipRect.height - 8; // 8px gap
    let left = iconRect.left + (iconRect.width - tooltipRect.width) / 2;

    // Check if tooltip would go above viewport
    if (top < 0) {
      // Position below the icon instead
      top = iconRect.bottom + 8;
    }

    // Check if tooltip would go beyond right edge
    if (left + tooltipRect.width > viewportWidth) {
      left = viewportWidth - tooltipRect.width - 16; // 16px margin
    }

    // Check if tooltip would go beyond left edge
    if (left < 0) {
      left = 16; // 16px margin
    }

    setTooltipPosition({ top: `${top}px`, left: `${left}px` });
  };

  const handleMouseEnter = () => {
    setShowTooltip(true);
    // Use setTimeout to ensure the tooltip is rendered before calculating position
    setTimeout(updateTooltipPosition, 0);
  };

  const handleMouseLeave = () => {
    setShowTooltip(false);
  };

  useEffect(() => {
    const handleResize = () => {
      if (showTooltip) {
        updateTooltipPosition();
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [showTooltip]);

  const tooltipElement = showTooltip && createPortal(
    <div 
      className="info-tooltip" 
      ref={tooltipRef}
      style={{
        position: 'fixed',
        top: tooltipPosition.top,
        left: tooltipPosition.left,
        zIndex: 10000,
      }}
    >
      {tooltip}
    </div>,
    document.body
  );

  return (
    <>
      <div 
        className="info-icon" 
        ref={iconRef} 
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        <svg 
          viewBox="0 0 24 24" 
          width="14" 
          height="14" 
          fill="none" 
          stroke="currentColor" 
          strokeWidth="2" 
          strokeLinecap="round" 
          strokeLinejoin="round"
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="16" x2="12" y2="12" />
          <line x1="12" y1="8" x2="12.01" y2="8" />
        </svg>
      </div>
      {tooltipElement}
    </>
  );
};

export default InfoIcon; 