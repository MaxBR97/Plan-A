import React, { useState } from 'react';
import SetInputBox from '../reusableComponents/SetInputBox.js';
import ParameterInputBox from '../reusableComponents/ParameterInputBox.js';
import "./ModuleBox.css";

const ModuleBox = ({
  key,
  module,
  prefcons,
  checked,
  handleToggleModule,
  handleAddTuple,
  handleRemoveTuple,
  handleTupleToggle,
  handleTupleChange,
  handleParamChange,
  isRowSelected,
  inputSets,
  inputParams
}) => {
  // Add state for minimized status
  const [minimized, setMinimized] = useState(false);

  // Toggle minimize/maximize
  const toggleMinimize = () => {
    setMinimized(!minimized);
  };

  return (
    <div key={key} className={`module-box ${minimized ? 'minimized' : ''}`}>
      <div className="module-header">
        {/* Toggle Button */}
        <div className="toggle-container">
          <label>
            <input
              type="checkbox"
              checked={checked}
              onChange={() => handleToggleModule(module.moduleName)}
            />
            <span></span>
          </label>
        </div>

        <h2 className="module-title">{module.moduleName}</h2>

        {/* Minimize/Maximize Button */}
        <button 
          className="minimize-button" 
          onClick={toggleMinimize}
          aria-label={minimized ? "Maximize" : "Minimize"}
        >
          {minimized ? "+" : "−"}
        </button>
      </div>

      {/* Content that gets hidden when minimized */}
      <div className="module-content">
        <p className="module-description">
          {module.description}
        </p>

        {/* Only render input sets section if there are input sets */}
        {inputSets && inputSets.length > 0 && (
          <div className="module-set-inputs">
            <h4>Input Sets:</h4>
            {inputSets.map((set, sIndex) => (
              <SetInputBox
                key={sIndex}
                index={sIndex}
                typeList={set.type}
                tupleTags={set.tags || []}
                setName={set.setName}
                handleAddTuple={handleAddTuple}
                handleTupleChange={handleTupleChange}
                handleTupleToggle={handleTupleToggle}
                handleRemoveTuple={handleRemoveTuple}
                isRowSelected={isRowSelected}
                setValues={set.setValues}
              />
            ))}
          </div>
        )}

        {/* Only render input parameters section if there are input parameters */}
        {inputParams && inputParams.length > 0 && (
          <div className="module-parameter-inputs">
            <h4>Input Parameters</h4>
            {inputParams.map((param, pIndex) => (
              <ParameterInputBox
                key={pIndex}
                paramName={param.paramName}
                type={param.type}
                value={param.value}
                onChange={handleParamChange}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ModuleBox;