import React, { useState, useEffect, useCallback } from "react";
import { useZPL } from "../context/ZPLContext";
import "./SolutionResultsPage.css";
import SuperTable from "../reusableComponents/SuperTable.js";
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import Checkbox from "../reusableComponents/Checkbox.js";

const SolutionResultsPage = () => {
  const {
    image,
    model,
    solutionResponse,
    updateImage,
    updateImageField,
    updateModel,
    updateSolutionResponse,
    initialImageState
  } = useZPL();
  const [selectedVariable, setSelectedVariable] = useState(null);
  const [displayValue, setDisplayValue] = useState(true);
  const [displayStructure, setDisplayStructure] = useState([]);
  
  // Add state for dynamic solutions
  const [dynamicSolutions, setDynamicSolutions] = useState({});
  
  // Add state for table edit mode
  const [tableEditMode, setTableEditMode] = useState(false);

  const getSetStructure = (variable) => {
    const varObj = image.variablesModule?.variablesOfInterest?.find((varObj) => varObj.identifier == variable);
    
    if(varObj?.tags && varObj?.tags?.length == varObj.type.length){
      return varObj.tags;
    }
    else
      return solutionResponse.solution[variable]?.setStructure || [];
  };

  const isBinary = (variable) => solutionResponse.solution[variable].solutions.every(
    (sol) => sol.objectiveValue < 0.00001 && sol.objectiveValue > -0.00001 || 
              sol.objectiveValue > 0.99999  && sol.objectiveValue < 1.00001
  );

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
    if (solutionResponse?.solved === false) {
      // Handle solved false case if needed
    }
    else if (solutionResponse?.solution) {
      const variables = Object.keys(solutionResponse.solution);
      if (variables.length > 0 && !selectedVariable) {
        const firstVariable = variables[0];
        const initialSetStructure = getSetStructure(firstVariable);
        const initialDisplayValue = !isBinary(firstVariable);
        
        // Batch these updates
        setSelectedVariable(firstVariable);
        setDisplayValue(initialDisplayValue);
        setDisplayStructure(initialDisplayValue 
          ? [...initialSetStructure, "value"] 
          : [...initialSetStructure]);
      }
    }
  }, [solutionResponse, selectedVariable]);

  // Early return conditions
  if(solutionResponse?.solved === false) {
    return <p>Failed to solve!</p>;
  }

  if (!solutionResponse || !solutionResponse.solution) {
    return <p>No solution data available.</p>;
  }

  // Only proceed with rendering the rest if we have a selected variable
  if (!selectedVariable || !dynamicSolutions[selectedVariable]) {
    return <p>Loading solution data...</p>;
  }

  const variableData = dynamicSolutions[selectedVariable];
  let { setStructure, solutions } = variableData;
  setStructure = getSetStructure(selectedVariable) || setStructure;

  const handleVariableChange = (event) => {
    const newVariable = event.target.value;
    const newDisplayValue = !isBinary(newVariable);
    const newSetStructure = getSetStructure(newVariable);
    
    // Update all related states in one batch
    setSelectedVariable(newVariable);
    setDisplayValue(newDisplayValue);
    setDisplayStructure(newDisplayValue 
      ? [...newSetStructure, "value"] 
      : newSetStructure);
  };

  function addObjectiveValueToSolutions(solutions) {
    return solutions.map(solution => {
      const { values, objectiveValue, ...rest } = solution;
      const newValues = [...values, objectiveValue];
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
  
  return (
    <div className="solution-results-page flex">
      <div className="w-2/3 pr-4">
        <h1 className="page-title text-2xl font-bold mb-4">Solution Results</h1>
        
        {/* Variable Selection and Table Controls */}
        <div className="solution-controls mb-4 flex flex-wrap items-center gap-4">
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
          
          <button 
            onClick={toggleTableEditMode}
            className={`px-3 py-1 rounded ${tableEditMode ? 'bg-blue-600 text-white' : 'bg-gray-200 hover:bg-gray-300'}`}
          >
            {tableEditMode ? 'Done Editing' : 'Change Table'}
          </button>
        </div>

        {/* Main Solution Table */}
        <div className="solution-table-container">
          <SuperTable 
            solutions={displayValue ? addObjectiveValueToSolutions(solutions) : solutions} 
            setStructure={displayValue ? [...setStructure, "value"] : setStructure}
            displayStructure={displayStructure}
            isDisplayBinary={!displayValue}
            valueSetName="value"
            editMode={tableEditMode}
            onSolutionUpdate={handleSolutionUpdate}
            onAddDimension={handleAddDimension}
          />
        </div>
      </div>

      {/* Draggable Set Structure Box */}
      <div className="w-1/3 pl-4 border-l">
        <h2 className="text-xl font-bold mb-3">Result Representation</h2>
        
        <div className="mb-4">
          <Checkbox
            label="Display Value Column"
            checked={displayValue}
            disabled={false}
            onChange={(checked) => handleDisplayValue(checked)}
            name="Display Value"
          />
        </div>
        
        <h3 className="font-semibold mb-2">Dimension Order (Drag to Reorder)</h3>
        <DragDropContext onDragEnd={onDragEnd}>
          <Droppable droppableId="set-structure-list">
            {(provided) => (
              <div 
                {...provided.droppableProps} 
                ref={provided.innerRef}
                className="space-y-2"
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
                        className="p-2 border rounded bg-gray-100 hover:bg-gray-200 cursor-move"
                      >
                        {set}
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
    </div>
  );
};

export default SolutionResultsPage;