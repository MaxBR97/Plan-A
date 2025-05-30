import React, { useState } from 'react';

const NumberInput = ({ 
  label = "", 
  value = 10, 
  onChange, 
  placeholder = "Enter Number",
  allowDecimals = true,
  min = -Infinity,
  max = Infinity,
  className = ""
}) => {
  const [inputValue, setInputValue] = useState(value || "");
  
  const handleChange = (e) => {
    const newValue = e.target.value;
    
    // Allow empty input for usability
    if (newValue === "") {
      setInputValue("");
      onChange && onChange("");
      return;
    }
    
    // Regex pattern for positive numbers with optional decimals
    const pattern = allowDecimals ? /^\d*\.?\d*$/ : /^\d+$/;
    
    if (pattern.test(newValue)) {
      const numValue = parseFloat(newValue);
      
      // Check if the number is within bounds (if it's a valid number)
      if (!isNaN(numValue) && numValue >= min && numValue <= max) {
        setInputValue(newValue);
        onChange && onChange(numValue);
      }
    }
  };

  return (
    <div className={`flex flex-col ${className}`}>
      {label && <label className="mb-1 text-sm font-medium">{label}</label>}
      <input
        type="text"
        value={inputValue}
        onChange={handleChange}
        placeholder={placeholder}
        className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        inputMode={allowDecimals ? "decimal" : "numeric"}
      />
    </div>
  );
};

export default NumberInput;