import React, { useState } from 'react';
import Checkbox from './Checkbox';
import "./SetEntry.css";

const SetEntry = ({ 
  label = "SetEntry",
  row,
  typeList,
  checked,
  onEdit,
  onToggle,
  onDelete,
  placeholder = ""
}) => {
  
  return (
    <div className="set-entry-container">
       <input 
        type="checkbox"
        className="variable-checkbox set-entry-element"
        onChange={onToggle}
        checked={checked}
        />
        {row.map((value, typeIndex) => {
        return (
          <input
            key={typeIndex} // Important to add a unique key here
            type="text"
            value={value}
            onChange={(e) => onEdit(e, typeIndex)} // Pass typeIndex here
            placeholder={`Enter ${typeList[typeIndex] || "value"}:`}
            className="set-entry-element"
          />
        );
      })}
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