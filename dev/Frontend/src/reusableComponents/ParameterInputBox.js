import React from "react";

const ParameterInputBox = ({ paramName, type, value, onChange }) => {
  return (
    <div className="module-box">
      <h3 className="module-title">{paramName}</h3>
      <p className="variable-type">
        <strong>Type:</strong> {type || "Unknown"}
      </p>
      <input
        type="text"
        value={value || ""}
        onChange={(e) => onChange(paramName, e.target.value)}
        className="variable-input"
        placeholder={`Enter ${type || "value"}...`}
      />
    </div>
  );
};

export default ParameterInputBox;
