import React, { useState, useEffect, useCallback } from "react";
import { useZPL } from "../context/ZPLContext";
import "./SolutionResultsPage.css";
import SuperTable from "../reusableComponents/SuperTable.js";
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import Checkbox from "../reusableComponents/Checkbox.js";
import NumberInput from '../reusableComponents/NumberInput';
import LogBoard from "../reusableComponents/LogBoard.js";

const SolutionResultsPage = ({ 
  image,
  variableValues,
  selectedVariableValues,
  paramValues,
  constraintsToggledOff,
  preferencesToggledOff,
  isDesktop = false
}) => {
  const {
    solutionResponse,
    updateSolutionResponse,
  } = useZPL();

  const [selectedVariable, setSelectedVariable] = useState(null);
  const [displayValue, setDisplayValue] = useState(true);
  const [displayStructure, setDisplayStructure] = useState([]);
  const [dynamicSolutions, setDynamicSolutions] = useState({});
  const [tableEditMode, setTableEditMode] = useState(false);
  const [globalSelectedTuples, setGlobalSelectedTuples] = useState({});
  
  // New state variables for solve-related functionality
  const [timeout, setTimeout] = useState(10);
  const [solutionStatus, setSolutionStatus] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [selectedScript, setSelectedScript] = useState(Object.keys(image.solverSettings)[0]);
  
  // Deep clone function for solutions
  const deepCloneSolutions = (solutions) => {
    return solutions.map(solution => ({
      ...solution,
      values: [...solution.values],
    }));
  };

  // Update solutions in dynamicSolutions
  const updateDynamicSolutions = useCallback((variable, newSolutions) => {
    console.log("updateDynamicSolutions", variable, newSolutions);
    setDynamicSolutions(prev => ({
      ...prev,
      [variable]: {
        ...prev[variable],
        solutions: newSolutions
      }
    }));
  }, []);

  // Handle solution updates from SuperTable
  const handleSolutionUpdate = (updatedSolutions) => {
    if (!selectedVariable) return;

    updateDynamicSolutions(selectedVariable, updatedSolutions);
  };

  // Toggle table edit mode
  const toggleTableEditMode = () => {
    setTableEditMode(prev => !prev);
  };

  // Handle deletion of selected tuples
  const handleDeleteSelected = () => {
    if (!selectedVariable || !globalSelectedTuples[selectedVariable]) return;
    
    const selectedTuples = globalSelectedTuples[selectedVariable];
    if (selectedTuples.length === 0) return;
    
    // Filter out the selected tuples from the current variable's solutions
    const updatedSolutions = dynamicSolutions[selectedVariable].solutions.filter(sol => 
      !selectedTuples.some(selected => 
        JSON.stringify(selected.values) === JSON.stringify(sol.values)
      )
    );
    
    // Update solutions
    updateDynamicSolutions(selectedVariable, updatedSolutions);
    
    // Remove the deleted tuples from selection
    setGlobalSelectedTuples(prev => ({
      ...prev,
      [selectedVariable]: []
    }));
  };

  // Update global selected tuples
  const updateSelectedTuples = (tuples) => {
    setGlobalSelectedTuples(prev => ({
      ...prev,
      [selectedVariable]: tuples
    }));
  };

  // Effect to sync dynamicSolutions with solutionResponse changes
  useEffect(() => {
    if (solutionResponse?.solution) {
      const newDynamicSolutions = {};
      
      Object.keys(solutionResponse.solution).forEach(varName => {
        newDynamicSolutions[varName] = {
          ...solutionResponse.solution[varName],
          solutions: deepCloneSolutions(solutionResponse.solution[varName].solutions)
        };
      });
      
      setDynamicSolutions(newDynamicSolutions);
    }
  }, [solutionResponse]);

  // Initialize selected variable and display structure
  useEffect(() => {
    if (solutionResponse?.solution) {
      const variables = Object.keys(solutionResponse.solution);
      // console.log("variables", variables, "selectedVariable", selectedVariable, "solutionResponse", solutionResponse);
      if (variables.length > 0 && !selectedVariable) {
        const firstVariable = variables[0];
        
        const initialSetStructure = getSetStructure(firstVariable);
        console.log("initialSetStructure", initialSetStructure);
        const initialDisplayValue = !isBinary(firstVariable);
        
        setSelectedVariable(firstVariable);
        setDisplayValue(initialDisplayValue);
        setDisplayStructure(initialDisplayValue 
          ? [...initialSetStructure, "value"] 
          : [...initialSetStructure]);
      }
    }
  }, [solutionResponse, selectedVariable]);

  const getSetStructure = (variable) => {
    const varObj = image.variablesModule?.variablesOfInterest?.find((varObj) => varObj.identifier == variable);
    
    if(varObj?.tags && varObj?.tags?.length == varObj.type.length){
      return varObj.tags;
    }
    else
      return solutionResponse?.solution?.[variable]?.setStructure || [];
  };

  const isBinary = (variable) => {
    if (!solutionResponse?.solution?.[variable]) return false;
    return solutionResponse.solution[variable].solutions.every( 
      (sol) => {
        return image.variablesModule.variablesOfInterest.find((varObj) => varObj.identifier == variable).isBinary
      }
    );
  };

  const variableData = dynamicSolutions[selectedVariable] || { solutions: [], setStructure: [] };
  const setStructure = selectedVariable ? getSetStructure(selectedVariable) : [];
  const solutions = variableData.solutions || [];

  const handleVariableChange = (event) => {
    const newVariable = event.target.value;
    if (!newVariable) return;
    
    const newDisplayValue = !isBinary(newVariable);
    const newSetStructure = getSetStructure(newVariable);
    
    setSelectedVariable(newVariable);
    setDisplayValue(newDisplayValue);
    setDisplayStructure(newDisplayValue 
      ? [...newSetStructure, "value"] 
      : newSetStructure);
  };

  function addObjectiveValueToSolutions(solutions) {
    return solutions.map(solution => {
      const { values, objectiveValue, ...rest } = solution;
      const newValues = objectiveValue ? [...values, objectiveValue] : [...values];
      return {
        ...rest,
        values: newValues
      };
    });
  }

  function extractObjectiveValueFromSolutions(solutions) {
    return solutions.map(solution => {
      const { values, ...rest } = solution;
      const newValues = [...values]; // clone to avoid mutating original
      const objectiveValue = newValues.pop(); // remove and save the last item
  
      return {
        ...rest,
        values: newValues,
        objectiveValue
      };
    });
  }

  const handleDisplayValue = (checked) => {
    setDisplayValue(checked);
    if(checked){
      setDisplayStructure([...displayStructure.filter(item => item !== "value"), "value"]);
    }
    else {
      setDisplayStructure(displayStructure.filter((entry) => entry !== "value"));
    }
  };

  const onDragEnd = (result) => {
    if (!result.destination) return;

    const newSetStructure = Array.from(displayStructure);
    const [reorderedItem] = newSetStructure.splice(result.source.index, 1);
    newSetStructure.splice(result.destination.index, 0, reorderedItem);
    setDisplayStructure(newSetStructure);
  };

  // Handle adding a new dimension to the table
  const handleAddDimension = (newDimension) => {
    // Add the new dimension to displayStructure
    const valuePosition = displayStructure.indexOf("value");
    let newDisplayStructure;
    
    if (valuePosition !== -1) {
      // If "value" is present, insert the new dimension before it
      newDisplayStructure = [
        ...displayStructure.slice(0, valuePosition),
        newDimension,
        ...displayStructure.slice(valuePosition)
      ];
    } else {
      // Otherwise, just append the new dimension
      newDisplayStructure = [...displayStructure, newDimension];
    }
    
    setDisplayStructure(newDisplayStructure);
  };

  const handleSelectScript = (key) => {
    setSelectedScript(key);
  };

  const handleSolve = async (isContinue = false) => {
    setErrorMessage(null);
    setSolutionStatus(null);
    
    // Transform param values
    const transformedParamValues = Object.fromEntries(
      Object.entries(paramValues).map(([key, value]) => [key, value])
    );

    // Initialize updatedSetsToValues with empty arrays for all sets
    const updatedSetsToValues = {};
    
    // First, initialize all non-bound sets with empty arrays
    Object.keys(variableValues).forEach(setName => {
      if (!image.variablesModule?.variablesOfInterest?.some(v => v.boundSet === setName)) {
        updatedSetsToValues[setName] = [];
      }
    });

    // Then fill in the selected values for non-bound sets
    Object.entries(variableValues).forEach(([setName, rows]) => {
      // Skip if this is a bound set
      if (image.variablesModule?.variablesOfInterest?.some(v => v.boundSet === setName)) {
        return;
      }

      if (selectedVariableValues[setName]) {
        const selectedRows = rows.filter((_, rowIndex) =>
          selectedVariableValues[setName].includes(rowIndex)
        );
        updatedSetsToValues[setName] = selectedRows;
      }
    });


    // Get all bound sets and their corresponding variables
    const boundSetToVariable = {};
    if (image.variablesModule?.variablesOfInterest) {
      image.variablesModule.variablesOfInterest.forEach(variable => {
        if (variable.boundSet) {
          boundSetToVariable[variable.boundSet] = variable.identifier;
        }
      });
    }

    // Initialize all bound sets with empty arrays
    Object.keys(boundSetToVariable).forEach(boundSetName => {
      updatedSetsToValues[boundSetName] = [];
    });

    // Then fill in the selected tuples for bound sets
    Object.entries(boundSetToVariable).forEach(([boundSetName, variableName]) => {
      const selectedTuples = globalSelectedTuples[variableName] || [];
      
      const boundSetValues = selectedTuples.map(tuple => {
        if (tuple.hasOwnProperty('objectiveValue') && tuple.objectiveValue !== undefined) {
          return [...tuple.values, tuple.objectiveValue];
        }
        return tuple.values;
      });

      updatedSetsToValues[boundSetName] = boundSetValues;
    });

    console.log("dynamicSolutions for request:", dynamicSolutions);
    console.log("globalSelectedTuples for request:", globalSelectedTuples);

    const requestBody = {
      imageId: image.imageId,
      input: {
        setsToValues: updatedSetsToValues,
        paramsToValues: transformedParamValues,
        constraintModulesToggledOff: constraintsToggledOff,
        preferenceModulesToggledOff: preferencesToggledOff,
      },
      timeout: timeout,
      solverSettings: image.solverSettings[selectedScript]
    };

    try {
      let startTime = Date.now();

      // Start a timer to update solutionStatus every second
      const timer = setInterval(async () => {
        setSolutionStatus("Solving " + ((Date.now() - startTime) / 1000).toFixed(1)); 
        if(isDesktop){ 
          const poll = await fetch("/solve/poll", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestBody),
          });
          window.appendLog(await poll.text() + "\n");
        }
      }, 1000);

      console.log("solve request:", requestBody);
      setShowModal(true);
      const response = await fetch(isContinue ? "/solve/continue" : isDesktop ? "/solve/start" : "/solve", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      clearInterval(timer);

      const responseText = await response.text();

      if (!response.ok) {
        console.error("Server returned an error:", responseText);
        throw new Error(`HTTP Error! Status: ${response.status} - ${responseText}`);
      }

      const data = JSON.parse(responseText);
      console.log("Solve response: ", data);

      // Clear selections if the solution is unsolved or has no solution data
      if (!data.solved || !data.solution) {
        setGlobalSelectedTuples({});
        setSelectedVariable(null);
        setDisplayStructure([]);
      }

      updateSolutionResponse(data);
      setSolutionStatus(data.solutionStatus);
    } catch (error) {
      console.error("Error solving problem:", error);
      setErrorMessage(`Failed to solve. ${error.message}`);
      // Also clear selections on error
      setGlobalSelectedTuples({});
      setSelectedVariable(null);
      setDisplayStructure([]);
    }
  };

  // Also add an effect to clear selections when solution becomes unsolved
  useEffect(() => {
    if (solutionResponse?.solved === false) {
      setGlobalSelectedTuples({});
      setSelectedVariable(null);
      setDisplayStructure([]);
    }
  }, [solutionResponse?.solved]);

  return (
    <div className="solution-results-page">
      {/* Control Panel */}
      <div className="control-panel">
        {/* Timeout Input */}
        <div className="control-panel-section">
          <div className="section-header">
            <h3 className="section-title">Timeout Setting</h3>
            <div className="info-icon">
              i
              <div className="info-tooltip">
                Maximum time (in seconds) allowed for the solver to find a solution
              </div>
            </div>
          </div>
          <div className="timeout-input">
            <label>Timeout (seconds):</label>
            <NumberInput 
              value={timeout}   
              onChange={setTimeout}
              placeholder="Enter amount"
              min="0"
            />
          </div>
        </div>

        {/* Solver Settings */}
        <div className="control-panel-section">
          <div className="section-header">
            <h3 className="section-title">Solver Settings</h3>
            <div className="info-icon">
              i
              <div className="info-tooltip">
                Choose the solver configuration to use for optimization
              </div>
            </div>
          </div>
          <div className="script-options">
            {Object.keys(image.solverSettings).map((key) => (
              <div key={key} className="script-option">
                <input
                  type="radio"
                  id={`script-${key}`}
                  name="solver-script"
                  checked={selectedScript === key}
                  onChange={() => handleSelectScript(key)}
                />
                <label htmlFor={`script-${key}`}>{key}</label>
              </div>
            ))}
          </div>
        </div>

        {/* Dimension Order */}
        <div className="control-panel-section">
          <div className="section-header">
            <h3 className="section-title">Dimension Order</h3>
            <div className="info-icon">
              i
              <div className="info-tooltip">
                Drag and drop to reorder how dimensions are displayed in the table
              </div>
            </div>
          </div>
          <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="set-structure-list">
              {(provided) => (
                <div 
                  {...provided.droppableProps} 
                  ref={provided.innerRef}
                  className="dimension-list"
                >
                  {displayStructure.map((set, index) => (
                    <Draggable 
                      key={`${set}-${index}`} 
                      draggableId={`${set}-${index}`} 
                      index={index}
                    >
                      {(provided) => (
                        <div
                          ref={provided.innerRef}
                          {...provided.draggableProps}
                          {...provided.dragHandleProps}
                          className="dimension-item"
                        >
                          <span className="dimension-name">{set}</span>
                          <span className="drag-handle">≡</span>
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </div>

        {/* Optimize Buttons */}
        <div className="control-panel-section">
          <div className="optimize-buttons">
            <button 
              className="optimize-button primary"
              onClick={() => handleSolve(false)}
            >
              Optimize
            </button>
            {isDesktop && (
              <button 
                className="optimize-button secondary"
                onClick={() => handleSolve(true)}
              >
                Continue Optimization
              </button>
            )}
          </div>

          {/* Solution Status */}
          {solutionStatus && (
            <div className="solution-status">
              <pre>{solutionStatus}</pre>
              {errorMessage && (
                <div className="error-message">
                  {errorMessage}
                </div>
              )}
            </div>
          )}

        </div>

        {/* LogBoard - Only show if isDesktop */}
        {isDesktop && (
          <div className="control-panel-section">
            <div className="section-header">
              <h3 className="section-title">Solution Log</h3>
              <div className="info-icon">
                i
                <div className="info-tooltip">
                  Detailed log of the optimization process
                </div>
              </div>
            </div>
            <LogBoard />
          </div>
        )}
      </div>

      {/* Table Area */}
      <div className="table-area">
        <div className="table-header">
          <div className="variable-selector">
            {solutionResponse?.solution && Object.keys(solutionResponse.solution).length > 0 && (
              <>
                <label>Select Variable:</label>
                <select 
                  onChange={handleVariableChange} 
                  value={selectedVariable || ''}
                >
                  {Object.keys(solutionResponse.solution).map((variable) => (
                    <option key={variable} value={variable}>
                      {variable}
                    </option>
                  ))}
                </select>
              </>
            )}
          </div>
          
          <div className="table-controls">
            <button 
              onClick={toggleTableEditMode}
              className={`edit-table-button ${tableEditMode ? 'active' : ''}`}
            >
              {tableEditMode ? 'Done Editing' : 'Edit Table'}
            </button>

            {tableEditMode && (
              <button 
                onClick={handleDeleteSelected}
                className="delete-selected-button"
                disabled={!globalSelectedTuples[selectedVariable] || globalSelectedTuples[selectedVariable].length === 0}
              >
                Delete Selected
              </button>
            )}
          </div>
        </div>

        <div className="table-container">
          <SuperTable 
            solutions={displayValue ? addObjectiveValueToSolutions(solutions) : solutions} 
            setStructure={displayValue ? [...setStructure, "value"] : setStructure}
            displayStructure={displayStructure}
            isDisplayBinary={!displayValue}
            valueSetName="value"
            editMode={tableEditMode}
            onSolutionUpdate={handleSolutionUpdate}
            onAddDimension={handleAddDimension}
            selectedTuples={globalSelectedTuples[selectedVariable] || []}
            onSelectedTuplesChange={updateSelectedTuples}
            defaultObjectiveValue={displayValue ? 0 : 1}
          />
        </div>
      </div>
    </div>
  );
};

export default SolutionResultsPage;