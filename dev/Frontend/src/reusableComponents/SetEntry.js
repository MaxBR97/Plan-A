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
    <div className="set-entry">
       <Checkbox 
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
            className="set-entry-atom"
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