import React from 'react';

const Checkbox = ({
  label,
  checked = false,
  disabled = false,
  onChange,
  name
}) => {
  return (
    <label className="checkbox-label" style={{
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      cursor: disabled ? 'not-allowed' : 'pointer',
      opacity: disabled ? 0.6 : 1
    }}>
      <input
        type="checkbox"
        name={name}
        checked={checked}
        disabled={disabled}
        onChange={(e) => onChange?.(e.target.checked)}
        style={{
          width: '16px',
          height: '16px',
          cursor: disabled ? 'not-allowed' : 'pointer'
        }}
      />
      {label && <span style={{ fontSize: '14px' }}>{label}</span>}
    </label>
  );
};

export default Checkbox;