import React, { useState } from 'react';
import SetInputBox from '../reusableComponents/SetInputBox.js';
import ParameterInputBox from '../reusableComponents/ParameterInputBox.js';

const ModuleBox = ({ 
  key,
  module,
  checked,
  handleToggleModule,
  handleAddTuple,
  handleRemoveTuple,
  handleTupleToggle,
  handleTupleChange,
  handleParamChange,
  isRowSelected,
  inputSets,
  inputParams,
}) => {
  

  return (
    <div key={key} className="module-box">
                {/* Toggle Button Positioned Correctly */}
                <div className="toggle-container">
                  <label className="switch">
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => handleToggleModule(module.name)}
                    />
                    <span className="slider round"></span>
                  </label>
                </div>

                <h3 className="module-title">{module.name}</h3>
                <p className="module-description">
                  <strong>Module Description:</strong> {module.description}
                </p>

                <h4 className="module-subtitle">Constraints</h4>
                {module.constraints.length > 0 ? (
                  module.constraints.map((constraint, cIndex) => (
                    <div key={cIndex} className="module-item">
                      <p>{constraint.identifier}</p>{" "}
                      {/* Only displaying the identifier value */}
                    </div>
                  ))
                ) : (
                  <p className="empty-message">
                    No constraints in this module.
                  </p>
                )}

                <h4 className="module-subtitle">Input Sets:</h4>
                {module.inputSets.length > 0 ? (
                  module.inputSets.map((set, sIndex) => (
                    <SetInputBox
                    index={sIndex}
                    typeList={inputSets[set].typeList}
                    tupleTags={inputSets[set].tupleTags || []}
                    setName={set}
                    handleAddTuple={handleAddTuple}
                    handleTupleChange={handleTupleChange}
                    handleTupleToggle={handleTupleToggle}
                    handleRemoveTuple={handleRemoveTuple}
                    isRowSelected={isRowSelected}
                    setValues={inputSets[set].setValues}
                    />
                  ))
                ) : (
                  <p className="empty-message">No Input Sets</p>
                )}

                <h4 className="module-subtitle">Input Parameters</h4>
                {module.inputParams.length > 0 ? (
                  module.inputParams.map((param, pIndex) => (
                    <ParameterInputBox
                        key={pIndex}
                        paramName={param}
                        type={inputParams[param].type}
                        value={inputParams[param].value}
                        onChange={handleParamChange}
                    />
                  ))
                ) : (
                  <p className="empty-message">No Input     Parameters.</p>
                )}
              </div>
        );
    };

export default ModuleBox;