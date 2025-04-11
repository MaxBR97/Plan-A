import React from 'react';
import './Checkbox.css';

const Checkbox = ({ label, checked, disabled, onChange, name }) => {
  const handleChange = (e) => {
    if (disabled) return;
    onChange(e.target.checked);
  };

  return (
    <div className={`checkbox ${disabled ? 'disabled' : ''}`}>
      <label>
        <input
          type="checkbox"
          checked={checked}
          disabled={disabled}
          onChange={handleChange}
          name={name}
        />
        <span className="label">{label}</span>
      </label>
    </div>
  );
};

export default Checkbox;