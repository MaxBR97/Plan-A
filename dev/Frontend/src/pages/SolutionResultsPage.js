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
  console.log("dynamicSolutions", dynamicSolutions);
  console.log("globalSelectedTuples", globalSelectedTuples);
  console.log("selectedVariableValues", selectedVariableValues);
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
    console.log("handleSolutionUpdate", updatedSolutions);
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
    console.log("updateSelectedTuples", tuples);
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

    console.log("updatedSetsToValues after non-bound sets:", updatedSetsToValues);

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
    console.log("bound sets mapping:", boundSetToVariable);
    console.log("final sets in request:", updatedSetsToValues);

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
    <div className="solution-results-page flex">
      <div className="w-2/3 pr-4">
        <h1 className="page-title text-2xl font-bold mb-4">Solution Results</h1>
        
        {/* Solve Controls Section - Always visible */}
        <div className="solve-controls mb-6">
          <div className="p-4">
            <NumberInput 
              value={timeout}   
              onChange={setTimeout}
              label="Timeout (seconds): "
              placeholder="Enter amount"
              min="0"
            />
          </div>

          <div className="script-options mb-4">
            <span className="font-semibold">Solver Settings: </span>
            {Object.keys(image.solverSettings).map((key) => (
              <div key={key} className="script-option inline-block ml-4">
                <input
                  type="radio"
                  id={`script-${key}`}
                  name="solver-script"
                  checked={selectedScript === key}
                  onChange={() => handleSelectScript(key)}
                  className="mr-2"
                />
                <label htmlFor={`script-${key}`}>{key}</label>
              </div>
            ))}
          </div>

          <div className="flex gap-4">
            <button 
              className="solve-button bg-blue-600 text-white px-4 py-2 rounded" 
              onClick={() => handleSolve(false)}
            >
              Optimize
            </button>
            {isDesktop && (
              <button 
                className="solve-button bg-green-600 text-white px-4 py-2 rounded" 
                onClick={() => handleSolve(true)}
              >
                Continue Optimization
              </button>
            )}
          </div>
        </div>

        {/* Table Controls - Always visible */}
        <div className="solution-controls mb-4 flex flex-wrap items-center gap-4">
          {solutionResponse?.solution && Object.keys(solutionResponse.solution).length > 0 && (
            <div className="solution-dropdown flex items-center">
              <label className="mr-2">Select Variable: </label>
              <select 
                onChange={handleVariableChange} 
                value={selectedVariable || ''}
                className="border rounded p-1"
              >
                {Object.keys(solutionResponse.solution).map((variable) => (
                  <option key={variable} value={variable}>
                    {variable}
                  </option>
                ))}
              </select>
            </div>
          )}
          
          <button 
            onClick={toggleTableEditMode}
            className={`px-3 py-1 rounded ${tableEditMode ? 'bg-blue-600 text-white' : 'bg-gray-200 hover:bg-gray-300'}`}
          >
            {tableEditMode ? 'Done Editing' : 'Edit Table'}
          </button>
          
          {tableEditMode && (
            <button 
              onClick={handleDeleteSelected}
              className="px-3 py-1 rounded bg-red-500 text-white hover:bg-red-600"
              disabled={!globalSelectedTuples[selectedVariable] || globalSelectedTuples[selectedVariable].length === 0}
            >
              Delete Selected
            </button>
          )}
        </div>

        {/* Main Solution Table - Always show, empty if no data */}
        <div className="solution-table-container max-h-[600px] overflow-auto">
          <div className="min-w-[800px]">
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

      {/* Draggable Set Structure Box - Always show */}
      <div className="w-1/3 pl-4 border-l">
        <h2 className="text-xl font-bold mb-3">Result Representation</h2>
        
        <h3 className="font-semibold mb-2">Dimension Order (Drag to Reorder)</h3>
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

      {/* Modal for Response - Only show when solving */}
      {showModal && (
        <div className="response-modal fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="modal-content bg-white p-6 rounded-lg max-w-2xl w-full mx-4">
            <span
              className="close-button absolute top-2 right-2 text-2xl cursor-pointer"
              onClick={() => setShowModal(false)}
            >
              ×
            </span>
            <h2 className="text-xl font-bold mb-4">Solution Status:</h2>
            <pre className="bg-gray-100 p-4 rounded">{solutionStatus}</pre>
            {errorMessage && (
              <div className="error-message text-red-600 mt-4">
                {errorMessage}
              </div>
            )}
            {isDesktop && <div className="mt-4"><LogBoard/></div>}
          </div>
        </div>
      )}
    </div>
  );
};

export default SolutionResultsPage;