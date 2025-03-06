import React from 'react';
import './TagConfigure.css'; 

const TagConfigure = ({ 
  label = "ConfigureTag",
  variable,
  types = [],
  values = [],
  onChange,
  placeholders = ""
}) => {
  // Ensure values is always an array
  const safeValues = Array.isArray(values) ? values : [];
  
  return (
    <div className="tag-configure-container">
      <div className="tag-header">
        <label className="tag-label">Tag variable <span className="tag-identifier">{variable.identifier}</span>:</label>
      </div>
      <div className="tag-inputs-container">
        {safeValues.map((val, index) => (
          <div className="tag-input-wrapper" key={index}>
            <input 
              className="tag-input"
              type="text"
              value={val}
              onChange={(e) => onChange(e, index)}
              placeholder={placeholders}
            />
            {index > 0 && (
              <span className="tag-input-separator">•</span>
            )}
          </div>
        ))}
        {safeValues.length === 0 && (
          <div className="tag-empty-message">No values available</div>
        )}
      </div>
    </div>
  );
};

export default TagConfigure;