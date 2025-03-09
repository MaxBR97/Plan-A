import React from "react";
import "./ParameterInputBox.css"

const ParameterInputBox = ({ paramName, type, value, onChange }) => {
  // Ensure the value is an array
  const paramValue = Array.isArray(value) ? value[0] || "" : "";

  return (
    <div className="parameter-input">
      <h3 className="parameter-name">{paramName}</h3>
      <p className="parameter-type">
        <strong>Type:</strong> {type || "Unknown"}
      </p>
      <input
        type="text"
        value={paramValue}
        onChange={(e) => onChange(paramName, [e.target.value])}
        className="parameter-entry-atom"
        placeholder={`Enter ${type || "value"}...`}
      />
    </div>
  );
};

export default ParameterInputBox;