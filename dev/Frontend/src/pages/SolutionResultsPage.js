import React, { useState, useEffect } from "react";
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
  // Use useEffect to update selectedVariable when solutionResponse becomes available

  const getSetStructure = (variable) => {
    const varObj = image.variablesModule?.variablesOfInterest?.find((varObj) => varObj.identifier == variable)
    
    if(varObj?.tags && varObj?.tags?.length == varObj.type.length){
      console.log(varObj.tags)
      return varObj.tags
    }
    else
      return solutionResponse.solution[variable]?.setStructure || [];
  }

  const isBinary = (variable) => solutionResponse.solution[variable].solutions.every(
    (sol) => sol.objectiveValue < 0.00001 && sol.objectiveValue > -0.00001 || 
              sol.objectiveValue > 0.99999  && sol.objectiveValue < 1.00001
  );

  useEffect(() => {
    
  }, [displayStructure]); 

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
  }, [solutionResponse]); 

  // Early return conditions
  if(solutionResponse?.solved == false) {
    return <p>Failed to solve!</p>;
  }

  if (!solutionResponse || !solutionResponse.solution) {
    return <p>No solution data available.</p>;
  }

  // Only proceed with rendering the rest if we have a selected variable
  if (!selectedVariable) {
    return <p>Loading solution data...</p>;
  }

  const variableData = solutionResponse.solution[selectedVariable];
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
  
      // Safety: make a copy of values so we don't mutate the original array
      const newValues = [...values, objectiveValue];
      // console.log("NEW VALS:",newValues)
      // console.log("NEW VALS2:",objectiveValue)
      // console.log("NEW VALS3:",solution)
      return {
        ...rest,       // any extra fields (if any)
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
    setDisplayValue(checked)
    if(checked){
      setDisplayStructure([...displayStructure, "value"])
      
      
    }
    else {
      setDisplayStructure(displayStructure.filter((entry) => entry !== "value"))
      
    }
  }

  const onDragEnd = (result) => {
    if (!result.destination) return;

    const newSetStructure = Array.from(displayStructure);
    const [reorderedItem] = newSetStructure.splice(result.source.index, 1);
    newSetStructure.splice(result.destination.index, 0, reorderedItem);
    setDisplayStructure(newSetStructure);
  };
  
  return (
    <div className="solution-results-page flex">
      <div className="w-2/3 pr-4">
        <h1 className="page-title text-2xl font-bold mb-4">Solution Results</h1>
        
        {/* Variable Dropdown */}
        <div className="solution-dropdown-container mb-4">
          <div className="solution-dropdown">
            <label className="mr-2">Select Variable: </label>
            <select 
  onChange={handleVariableChange} 
  value={selectedVariable || ''}
  className="border rounded p-1"
  onClick={(e) => {
    e.currentTarget.focus();
    //e.stopPropagation();
  }}
>
  {Object.keys(solutionResponse.solution).map((variable) => (
    <option key={variable} value={variable}>
      {variable}
    </option>
  ))}
</select>
          </div>
        </div>

        {/* Main Solution Table */}
        <div className="solution-table-container">
          <SuperTable 
            solutions={displayValue ? addObjectiveValueToSolutions(solutions) : solutions} 
            setStructure={displayValue ? [...setStructure, "value"] : setStructure}
            displayStructure={displayStructure}
            onValueChange={(tuple, newValue) => { 
              console.log("Updated:", tuple, "->", newValue); 
            }} 
          />
        </div>
      </div>

      {/* Draggable Set Structure Box */}
      
        <div>
          <h2>Result Representation Order</h2>
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
          <Checkbox
            label="Display Value?"
            checked={displayValue}
            disabled={false}
            onChange={(checked) => handleDisplayValue(checked)}
            name="Display Value"
            />
        </div>
      
    </div>
  );
};

export default SolutionResultsPage;