import React from 'react';
import './InfoIcon.css';

const InfoIcon = ({ tooltip }) => {
  return (
    <div className="info-icon">
      i
      <div className="info-tooltip">
        {tooltip}
      </div>
    </div>
  );
};

export default InfoIcon; 