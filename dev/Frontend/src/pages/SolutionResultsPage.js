import React, { useState, useEffect } from "react";
import { useZPL } from "../context/ZPLContext";
import "./SolutionResultsPage.css";
import SuperTable from "../reusableComponents/SuperTable.js";
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import Checkbox from "../reusableComponents/Checkbox.js";

const SolutionResultsPage = () => {
  const { solutionResponse } = useZPL();
  const [selectedVariable, setSelectedVariable] = useState(null);
  const [displayValue, setDisplayValue] = useState(true);
  const [displayStructure, setDisplayStructure] = useState([]);
  // Use useEffect to update selectedVariable when solutionResponse becomes available
  useEffect(() => {
    if(solutionResponse?.solved == false) {
      // Handle solved false case if needed
    }
    else if (solutionResponse?.solution) {
      const variables = Object.keys(solutionResponse.solution);
      if (variables.length > 0 && !selectedVariable) {
        const firstVariable = variables[0];
        setSelectedVariable(firstVariable);
        
        // Initialize displayStructure with the first variable's set structure
        const initialSetStructure = solutionResponse.solution[firstVariable]?.setStructure || [];
        
        // Only set initial state if displayValue hasn't been set yet
        if (displayValue === null) {
          setDisplayValue(true);
        }
        
        // Always update displayStructure to include 'value'
        setDisplayStructure([...initialSetStructure, "value"]);
      }
    }
  }, [solutionResponse, selectedVariable, displayValue]);

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

  const handleVariableChange = (event) => {
    const newVariable = event.target.value;
    setSelectedVariable(newVariable);
    
    // Update displayStructure when variable changes
    const newSetStructure = solutionResponse.solution[newVariable]?.setStructure || [];
    if(displayValue)
      setDisplayStructure([...newSetStructure, "value"]);
    else
    setDisplayStructure(newSetStructure);
  };

  const handleDisplayValue = (checked) => {
    setDisplayValue(checked)
    if(checked)
      setDisplayStructure([...displayStructure, "value"])
    else
      setDisplayStructure(displayStructure.filter((entry) => entry !== "value"))
  }

  const variableData = solutionResponse.solution[selectedVariable];
  const { setStructure, solutions } = variableData;

  // Check if all objective values are binary (0 or 1)
  const isBinary = solutions.every(
    (sol) => sol.objectiveValue === 0 || sol.objectiveValue === 1
  );

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
            solutions={solutions} 
            setStructure={setStructure}
            displayStructure={displayStructure}
            isBinary={true} 
            valueSetName={"value"}
            onValueChange={(tuple, newValue) => { 
              console.log("Updated:", tuple, "->", newValue); 
            }} 
          />
        </div>
      </div>

      {/* Draggable Set Structure Box */}
      <div className="w-1/3 pl-4">
        <div className="border rounded p-4">
          <h2 className="text-xl font-semibold mb-4">Result Representation Order</h2>
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
    </div>
  );
};

export default SolutionResultsPage;