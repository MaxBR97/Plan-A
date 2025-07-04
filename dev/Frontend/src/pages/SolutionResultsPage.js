import React, { useState, useEffect, useCallback, useRef, useMemo } from "react";
import { useZPL } from "../context/ZPLContext";
import "./SolutionResultsPage.css";
import SuperTable from "../reusableComponents/SuperTable.js";
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import Checkbox from "../reusableComponents/Checkbox.js";
import NumberInput from '../reusableComponents/NumberInput';
import LogBoard from "../reusableComponents/LogBoard.js";
import ErrorDisplay from '../components/ErrorDisplay';
import InfoIcon from '../reusableComponents/InfoIcon';
import axios from 'axios';

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
    resetSolutionResponse,
  } = useZPL();

  const pollIntervalMillisec = 3000;
  const [selectedVariable, setSelectedVariable] = useState(image?.variablesModule?.variablesOfInterest[0]?.identifier || undefined);
  const [displayValue, setDisplayValue] = useState(!(image?.variablesModule?.variablesOfInterest[0]?.isBinary));
  const [customDisplayStructure, setCustomDisplayStructure] = useState(null);
  const [dynamicSolutions, setDynamicSolutions] = useState({});
  const [tableEditMode, setTableEditMode] = useState(false);
  const [globalSelectedTuples, setGlobalSelectedTuples] = useState({});
  // New state variables for solve-related functionality
  const [timeout, setTimeout] = useState(10);
  const [solutionStatus, setSolutionStatus] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [logText, setLogText] = useState('');

  const [selectedScript, setSelectedScript] = useState(() => {
    // Find the "Default" or "default" key in solverSettings
    const defaultKey = Object.keys(image.solverSettings).find(key => 
      key.toLowerCase() === "default"
    );
    // If found, use it; otherwise fallback to first key
    return defaultKey || Object.keys(image.solverSettings)[0];
  });

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

  // Function to process binary variables in solution response
  const processBinaryVariables = (solutionData) => {
    if (!solutionData || !solutionData.solution) return solutionData;

    const processedSolution = { ...solutionData };
    
    Object.keys(processedSolution.solution).forEach(varName => {
      const varData = processedSolution.solution[varName];
      
      // Check if this is a binary variable by looking at the variable definition
      const isBinaryVar = image?.variablesModule?.variablesOfInterest?.find(
        v => v.identifier === varName
      )?.isBinary;
      
      if (isBinaryVar && varData.solutions) {
        // Filter and process binary variable solutions
        const processedSolutions = varData.solutions
          .filter(solution => {
            // Keep solutions with objective value >= 0.5
            return solution.objectiveValue >= 0.5;
          })
          .map(solution => ({
            ...solution,
            // Round objective values >= 0.5 to 1
            objectiveValue: solution.objectiveValue >= 0.5 ? 1 : 0
          }));
        
        processedSolution.solution[varName] = {
          ...varData,
          solutions: processedSolutions
        };
      }
    });
    
    return processedSolution;
  };

  // Function to check if variable has a bound set
  const hasVariableBoundSet = (variable) => {
    if (!variable || !image.variablesModule?.variablesOfInterest) return false;
    const varObj = image.variablesModule.variablesOfInterest.find(v => v.identifier === variable);
    return varObj && varObj.boundSet;
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
      if (variables.length > 0) {
    
        // Always set the first variable if none is selected or if the current selection is not valid
        const firstVariable = variables[0];
        const shouldSetVariable = !selectedVariable || !variables.includes(selectedVariable);
        
        if (shouldSetVariable) {
          const initialDisplayValue = !isBinary(firstVariable);
          setDisplayValue(initialDisplayValue);
          setSelectedVariable(firstVariable);
          setCustomDisplayStructure(null);
        }
      }
    }
  }, [image, solutionResponse]);
  
  // Reset solutionResponse to default state on component load
  useEffect(() => {
    resetSolutionResponse();
  }, []); // Empty dependency array means this runs only once on mount
  
  const getSetStructure = (variable) => {
    const varObj = image.variablesModule?.variablesOfInterest?.find((varObj) => varObj.identifier == variable);
    if(varObj?.tags && varObj?.tags?.length == varObj.type.length){
      return varObj.tags;
    }
    else{
      return varObj.type;
    }
  };

  const isBinary = (variable) => {
    console.log("isBinary:", variable, image.variablesModule.variablesOfInterest.find((varObj) => varObj.identifier == variable).isBinary);
    return image.variablesModule.variablesOfInterest.find((varObj) => varObj.identifier == variable).isBinary
  };

  // Use useMemo to ensure React tracks these values for re-renders
  const variableData = useMemo(() => {
    return dynamicSolutions[selectedVariable] || { solutions: [], setStructure: [] };
  }, [dynamicSolutions, selectedVariable]);

  const setStructure = useMemo(() => {
    return selectedVariable ? getSetStructure(selectedVariable) : [];
  }, [selectedVariable, image]);

  const solutions = useMemo(() => {
    return variableData.solutions || [];
  }, [variableData]);

  // Compute displayStructure from setStructure and displayValue, or use custom if set
  const displayStructure = useMemo(() => {
    if (customDisplayStructure) {
      return customDisplayStructure;
    }
    
    if (!setStructure || setStructure.length === 0) {
      return [];
    }
    
    return displayValue 
      ? [...setStructure, "value"] 
      : [...setStructure];
  }, [setStructure, displayValue, customDisplayStructure]);

  // console.log("solutions", solutions);
  // console.log("selectedVariable", selectedVariable);
  // console.log("setStructure", setStructure);
  // console.log("displayStructure", displayStructure);
  // console.log("variableData", variableData);
  // console.log("dynamicSolutions", dynamicSolutions);
  const handleVariableChange = (event) => {
    const newVariable = event.target.value;
    if (!newVariable) return;
    
    const newDisplayValue = !isBinary(newVariable);
    
    setSelectedVariable(newVariable);
    setDisplayValue(newDisplayValue);
    // Clear custom display structure when variable changes
    setCustomDisplayStructure(null);
  };

  function addObjectiveValueToSolutions(solutions) {
    return solutions.map(solution => {
      const { values, objectiveValue, ...rest } = solution;
      // const newValues = objectiveValue ? [...values, objectiveValue] : [...values];
      const newValues = [...values, objectiveValue];
      return {
        ...rest,
        values: newValues
      };
    });
  }

  const onDragEnd = (result) => {
    if (!result.destination) return;

    const newSetStructure = Array.from(displayStructure);
    const [reorderedItem] = newSetStructure.splice(result.source.index, 1);
    newSetStructure.splice(result.destination.index, 0, reorderedItem);
    setCustomDisplayStructure(newSetStructure);
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
    
    setCustomDisplayStructure(newDisplayStructure);
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
    setErrorMessage(null);
    setPreviousStatus(solutionStatus);
    setSolutionStatus(null);
    setIsSolving(true);
    requestCancelledRef.current = false;
    if(!isContinue){
      setLogText('');
    }

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

    let timer = null;
    try {
      let startTime = Date.now();
      let lastPollTime = startTime;
      let isPolling = false;

      // Start a timer to update solutionStatus every second
      timer = setInterval(async () => {
        if (!requestCancelledRef.current) {  // Only update status if not cancelled
          setSolutionStatus("Solving " + ((Date.now() - startTime) / 1000).toFixed(1)); 
          if(isDesktop){
            const currentTime = Date.now();
            if(currentTime - lastPollTime >= pollIntervalMillisec && !isPolling){
              try {
                isPolling = true;
                console.log("Polling");
                const poll = await axios.post("/solve/poll", requestBody, {
                  signal: currentAbortController.current.signal
                });
                console.log("poll", poll.data);
                const pollDataString = JSON.stringify(poll.data);
                if (pollDataString && pollDataString.trim() !== '' && pollDataString !== '""') {
                  // Remove surrounding quotes and replace \n with actual newlines
                  const cleanText = pollDataString.replace(/^"|"$/g, '').replace(/\\n/g, '\n');
                  setLogText(cleanText);
                }
                lastPollTime = currentTime;
              } catch (error) {
                if (error.name === 'AbortError') {
                  console.log('Poll request aborted');
                }
              } finally {
                isPolling = false;
              }
            }
          }
        }
      }, 100);
      setSolveTimer(timer);

      setShowModal(true);
      let solveUrl = "/api/solve";
      // If you have /api/solve/continue or /api/solve/start, adjust here
      if (isContinue) solveUrl = "/solve/continue";
      else if (isDesktop) solveUrl = "/solve/start";
      const response = await axios.post(solveUrl, requestBody, {
        signal: currentAbortController.current.signal
      });

      // Always clear the timer after solve completes
      if (timer) {
        clearInterval(timer);
        setSolveTimer(null);
      }

      // If request was cancelled or this isn't the current request, don't process the response
      if (requestCancelledRef.current || currentRequestId.current !== requestId) {
        setIsSolving(false);
        return;
      }

      setIsSolving(false);

      const data = response.data;
      console.log("solution received", data);
      // Only process response if request wasn't cancelled and this is still the current request
      if (!requestCancelledRef.current && currentRequestId.current === requestId) {
        if (!data.solved || !data.solution) {
          setGlobalSelectedTuples({});
        }

        // Process binary variables before updating the solution response
        const processedData = processBinaryVariables(data);
        updateSolutionResponse(processedData);
        setSolutionStatus("Solution Status: " + processedData.solutionStatus);
      }
    } catch (error) {
      // Always clear the timer on error
      if (timer) {
        clearInterval(timer);
        setSolveTimer(null);
      }
      // Only show error if request wasn't cancelled and this is still the current request
      if (!requestCancelledRef.current && currentRequestId.current === requestId) {
        if (error.name !== 'AbortError') {
          console.error(error);
          try {
            // Try to parse the error message from the Error object
            let errorObj = error.response?.data;
            if (!errorObj && error.message) {
              try {
                errorObj = JSON.parse(error.message);
              } catch (e) {
                errorObj = { msg: error.message };
              }
            }
            setErrorMessage(errorObj);
          } catch (e) {
            // If parsing fails, create a simple error object
            setErrorMessage({ msg: error.message });
          }
          setGlobalSelectedTuples({});
        }
        setIsSolving(false);
      }
    } finally {
      // Defensive: always clear the timer in finally
      if (timer) {
        clearInterval(timer);
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
  if (Object.keys(dynamicSolutions).length === 0) return;

  const cleanedSelectedTuples = {};

  Object.entries(globalSelectedTuples).forEach(([variable, selectedTuples]) => {
    if (!dynamicSolutions[variable]) return;

    const currentSolutions = dynamicSolutions[variable].solutions;

    const validSelectedTuples = selectedTuples.filter(selectedTuple =>
      currentSolutions.some(solution => {
        const selectedValues = selectedTuple.values;
        const solutionValues = solution.values;

        // Helper to normalize value for loose comparison
        const normalize = (v) => (v === null || v === undefined) ? v : v.toString();

        // Case 1: Exact match
        const exactMatch =
          selectedValues.length === solutionValues.length &&
          selectedValues.every((value, index) => normalize(value) === normalize(solutionValues[index]));

        // Case 2: Match with objectiveValue appended
        const extendedMatch =
          selectedValues.length === solutionValues.length + 1 &&
          selectedValues.slice(0, solutionValues.length).every((value, index) =>
            normalize(value) === normalize(solutionValues[index])
          ) &&
          normalize(selectedValues[selectedValues.length - 1]) === normalize(solution.objectiveValue);

        return exactMatch || extendedMatch;
      })
    );

    if (validSelectedTuples.length > 0) {
      cleanedSelectedTuples[variable] = validSelectedTuples;
    }
  });

  setGlobalSelectedTuples(cleanedSelectedTuples);
}, [dynamicSolutions]);

  return (
    <div className="solution-results-page">
      {/* Control Panel */}
      <div className="control-panel">
        {/* Timeout Input */}
        <div className="control-panel-section">
          <div className="section-header">
            <h3 className="section-title">Timeout Setting</h3>
            <InfoIcon tooltip="Maximum time (in seconds) allowed for the solver to find a solution. The more time it is given, the better solution it can find or more likely to find a solution." />
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
            <h3 className="section-title">Solver Emphasis</h3>
            <InfoIcon tooltip="Choose a solver configuration to use for optimization. Different settings might be relevant for different inputs and goals." />
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
            <InfoIcon tooltip="Drag and drop the headers to reorder the way they are displayed in the solution table." />
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
              <h3 className="section-title">Solver Log</h3>
              <InfoIcon tooltip="Detailed log of the optimization process" />
              
            </div>
            <LogBoard text={logText} />
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
                {selectedVariable && !hasVariableBoundSet(selectedVariable) && (
                  <div className="selection-disabled-message">
                    Selection for this variable is disabled, as it has no bound set
                  </div>
                )}
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
                disabled={!hasVariableBoundSet(selectedVariable) || !globalSelectedTuples[selectedVariable] || globalSelectedTuples[selectedVariable].length === 0}
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
            selectionDisabled={!hasVariableBoundSet(selectedVariable)}
          />
        </div>
      </div>
    </div>
  );
};

export default SolutionResultsPage;