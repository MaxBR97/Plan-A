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
  inputSets = module.inputSets,
  inputParams = module.inputParams,
  variableValues,
  paramValues
}) => {
  // Add state for minimized status
  const [minimized, setMinimized] = useState(false);
  const [nonCostParams, setNonCostParams] = useState(inputParams.filter(param => !module.costParams.some(costParam => costParam.name === param.name)));
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
            <h4>Input Sets</h4>
            {inputSets.map((set, sIndex) => ( 
              <SetInputBox
                key={sIndex}
                index={sIndex}
                typeList={set.type}
                tupleTags={set.tags || []}
                setName={set.name}
                setAlias={set.alias}
                handleAddTuple={handleAddTuple}
                handleTupleChange={handleTupleChange}
                handleTupleToggle={handleTupleToggle}
                handleRemoveTuple={handleRemoveTuple}
                isRowSelected={isRowSelected}
                setValues={variableValues[set.name] || []}
              />
            ))}
          </div>
        )}

        {/* Only render input parameters section if there are input parameters */}
        {nonCostParams && nonCostParams.length > 0 && (
          <div className="module-parameter-inputs">
            <h4>Input Parameters</h4>
            {nonCostParams.map((param, pIndex) => (
              <ParameterInputBox
                key={pIndex}
                paramName={param.paramName}
                paramAlias={param.alias}
                type={param.type}
                value={paramValues[param.name] || ""}
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