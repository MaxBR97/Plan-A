import React, { useState } from 'react';
import Checkbox from './Checkbox';

const SetEntry = ({ 
  label = "SetEntry",
  type,
  value = 10, 
  checked,
  onEdit,
  onToggle,
  onDelete,
  placeholder = ""
}) => {
  
  return (
    <div>
       <input
        type="checkbox"
        className="variable-checkbox"
        onChange={onToggle}
        checked={checked}
        />
        <input
        type={type}
        value={value}
        onChange={onEdit}
        placeholder={placeholder}
        />
        <button 
        className="delete-button"
        onClick={onDelete}
        >
            X
        </button>
  </div>
  );
};

export default SetEntry;