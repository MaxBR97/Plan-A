import React, { useState, useEffect, useCallback, useRef } from "react";
import { useZPL } from "../context/ZPLContext";
import "./SolutionResultsPage.css";
import SuperTable from "../reusableComponents/SuperTable.js";
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import Checkbox from "../reusableComponents/Checkbox.js";
import NumberInput from '../reusableComponents/NumberInput';
import LogBoard from "../reusableComponents/LogBoard.js";
import ErrorDisplay from '../components/ErrorDisplay';

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

  const pollIntervalMillisec = 1000;
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
  const [isSolving, setIsSolving] = useState(false);
  const [previousStatus, setPreviousStatus] = useState(null);
  const [solveTimer, setSolveTimer] = useState(null);
  const requestCancelledRef = useRef(false);
  const currentAbortController = useRef(null);
  const currentRequestId = useRef(null);
  // console.log("globalSelectedTuples", globalSelectedTuples);
  // console.log("dynamicSolutions", dynamicSolutions);
  // Deep clone function for solutions
  const deepCloneSolutions = (solutions) => {
    return solutions.map(solution => ({
      ...solution,
      values: [...solution.values],
    }));
  };

  // Update solutions in dynamicSolutions
  const updateDynamicSolutions = useCallback((variable, newSolutions) => {
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
    if (image?.variablesModule?.variablesOfInterest) {
      const variables = image.variablesModule.variablesOfInterest.map(v => v.identifier);
      if (variables.length > 0 && !selectedVariable) {
        const firstVariable = variables[0];
        
        const initialSetStructure = getSetStructure(firstVariable);
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
      return varObj.type;
  };

  const isBinary = (variable) => {
    return image.variablesModule.variablesOfInterest.find((varObj) => varObj.identifier == variable).isBinary
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

  const cancelSolve = () => {
    console.log("Cancelling solve", currentRequestId.current);
    // Clear the timer
    if (solveTimer) {
      clearInterval(solveTimer);
      setSolveTimer(null);
    }
    
    // Abort the fetch request
    if (currentAbortController.current) {
      currentAbortController.current.abort();
    }
    
    // Mark request as cancelled
    requestCancelledRef.current = true;
    
    // Reset states
    setIsSolving(false);
    setSolutionStatus(previousStatus);
    setShowModal(false);
    setErrorMessage(null); // Clear any error messages when cancelling
  };

  const handleSolve = async (isContinue = false) => {
    console.log("handleSolve");
    setErrorMessage(null);
    setPreviousStatus(solutionStatus);
    setSolutionStatus(null);
    setIsSolving(true);
    requestCancelledRef.current = false;

    // Create new AbortController for this request
    currentAbortController.current = new AbortController();
    const requestId = new Date().getTime().toString();
    currentRequestId.current = requestId;
    
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

    console.log("solve request:", requestBody);

    try {
      let startTime = Date.now();

      // Start a timer to update solutionStatus every second
      let i = 0
      const timer = setInterval(async () => {

        if (!requestCancelledRef.current) {  // Only update status if not cancelled
          setSolutionStatus("Solving " + ((Date.now() - startTime) / 1000).toFixed(1)); 
          if(isDesktop){
             
            try {
              const poll = await fetch("/solve/poll", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(requestBody),
                signal: currentAbortController.current.signal
              });
              if((i*100) % pollIntervalMillisec == 0)
                 window.appendLog(await poll.text() + "\n");
            } catch (error) {
              if (error.name === 'AbortError') {
                console.log('Poll request aborted');
              }
            }
            i++;
          }
        }
      }, 100);
      
      setSolveTimer(timer);

      setShowModal(true);
      const response = await fetch(
        isContinue ? "/solve/continue" : isDesktop ? "/solve/start" : "/solve", 
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(requestBody),
          signal: currentAbortController.current.signal
        }
      );

      clearInterval(timer);
      setSolveTimer(null);

      // If request was cancelled or this isn't the current request, don't process the response
      if (requestCancelledRef.current || currentRequestId.current !== requestId) {
        // console.log("Request was cancelled or superseded, ignoring response", requestId);
        setIsSolving(false);
        return;
      }

      setIsSolving(false);

      const responseText = await response.text();

      if (!response.ok) {
        console.error("Server returned an error:", responseText);
        let errorMessage = responseText;
        try {
          // Try to parse the error message as JSON
          const errorData = JSON.parse(responseText);
          errorMessage = errorData; // Pass the complete error object
        } catch (e) {
          // If parsing fails, create a simple error object
          errorMessage = { msg: responseText };
        }
        throw new Error(JSON.stringify(errorMessage));
      }

      const data = JSON.parse(responseText);
      console.log("solution received", data);
      // Only process response if request wasn't cancelled and this is still the current request
      if (!requestCancelledRef.current && currentRequestId.current === requestId) {
        if (!data.solved || !data.solution) {
          setGlobalSelectedTuples({});
        }

        updateSolutionResponse(data);
        setSolutionStatus("Solution Status: " + data.solutionStatus);
      }
    } catch (error) {
      // Only show error if request wasn't cancelled and this is still the current request
      if (!requestCancelledRef.current && currentRequestId.current === requestId) {
        if (error.name !== 'AbortError') {
          console.error(error);
          try {
            // Try to parse the error message from the Error object
            const errorObj = JSON.parse(error.message);
            setErrorMessage(errorObj);
          } catch (e) {
            // If parsing fails, create a simple error object
            setErrorMessage({ msg: error.message });
          }
          setGlobalSelectedTuples({});
        }
      }
      setIsSolving(false);
      if (solveTimer) {
        clearInterval(solveTimer);
        setSolveTimer(null);
      }
    }
  };

  // Also add an effect to clear selections when solution becomes unsolved
  useEffect(() => {
    if (solutionResponse?.solved === false) {
      setGlobalSelectedTuples({});

    }
  }, [solutionResponse?.solved]);

  // Add this new effect to clean up globalSelectedTuples
  useEffect(() => {
    // Skip if dynamicSolutions is empty
    if (Object.keys(dynamicSolutions).length === 0) return;

    // Create a new object to hold the cleaned up selections
    const cleanedSelectedTuples = {};

    // For each variable in globalSelectedTuples
    Object.entries(globalSelectedTuples).forEach(([variable, selectedTuples]) => {
      // Skip if this variable doesn't exist in dynamicSolutions
      if (!dynamicSolutions[variable]) return;

      // Filter out tuples that no longer exist in dynamicSolutions
      const currentSolutions = dynamicSolutions[variable].solutions;
      const validSelectedTuples = selectedTuples.filter(selectedTuple => 
        currentSolutions.some(solution => {
          // Compare values arrays
          const selectedValues = selectedTuple.values;
          const solutionValues = solution.values;
          
          // If lengths are different, they can't be equal
          if (selectedValues.length !== solutionValues.length) return false;
          
          // Compare each value
          return selectedValues.every((value, index) => value === solutionValues[index]);
        })
      );

      // Only add to cleaned object if there are valid selections
      if (validSelectedTuples.length > 0) {
        cleanedSelectedTuples[variable] = validSelectedTuples;
      }
    });

    // Update globalSelectedTuples with the cleaned version
    setGlobalSelectedTuples(cleanedSelectedTuples);
  }, [dynamicSolutions]); // Only run when dynamicSolutions changes

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
              placeholder="Seconds"
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
          {/* Solution Status */}
          {(solutionStatus || errorMessage) && (
            <div className="solution-status">
              {solutionStatus && <pre>{solutionStatus}</pre>}
              {errorMessage && (
                <ErrorDisplay 
                  error={errorMessage} 
                  onClose={() => setErrorMessage(null)}
                  
                />
              )}
            </div>
          )}

          <div className="optimize-buttons">
            {!isSolving ? (
              <>
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
              </>
            ) : (
              <button 
                className="cancel-button"
                onClick={cancelSolve}
              >
                Cancel
              </button>
            )}
          </div>
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
      <div className={`table-area ${isSolving ? 'optimizing' : ''}`}>
        <div className="table-header">
          <div className="variable-selector">
            {image?.variablesModule?.variablesOfInterest && image.variablesModule.variablesOfInterest.length > 0 && (
              <>
                <label>Select Variable:</label>
                <select 
                  onChange={handleVariableChange} 
                  value={selectedVariable || ''}
                >
                  {image.variablesModule.variablesOfInterest.map((variable) => (
                    <option key={variable.identifier} value={variable.identifier}>
                      {variable.identifier}
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