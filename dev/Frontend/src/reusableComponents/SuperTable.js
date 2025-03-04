import React, { useState } from "react";

const SuperTable = ({ solutions, setStructure, isBinary, onValueChange, showTitleOnLastSet=true }) => {
  const [editingCell, setEditingCell] = useState(null); // Track which cell is being edited
  const [editValue, setEditValue] = useState(""); // Temporary value for editing

  if (setStructure.length < 1) {
    return <p>Set structure must have at least 1 dimension.</p>;
  }

  const handleCellClick = (solutionTuple) => {
    console.log(solutionTuple)
    setEditingCell(solutionTuple); // Store the full tuple being edited
    setEditValue(solutionTuple.objectiveValue); // Set current value for editing
  };

  

  const generateTable = (level, parentFilters = {}) => {
    // console.log(setStructure)
    const currentSet = setStructure[level];
    const nextSet = setStructure[level + 1];

    const uniqueValues = [
      ...new Set(
        solutions
          .filter((sol) =>
            Object.entries(parentFilters).every(([key, val]) => {
              const keyIndex = setStructure.indexOf(key);
              return keyIndex !== -1 && sol.values[keyIndex] === val;
            })
          )
          .map((sol) => sol.values[level])
      ),
    ].sort((a, b) => {
      if (typeof a === "number" && typeof b === "number") {
        return a - b; // Sort numbers in ascending order
      }
      return String(a).localeCompare(String(b)); // Sort text alphabetically
    });
    
    
    // if(level === setStructure.length) {
    //   console.log("ALERT1")
    //   return( <table></table> )
    // }

    if (level === setStructure.length - 1) {
      return (
        <table className="solution-table">
          <thead>
          { 
            showTitleOnLastSet ?
            <tr>
              <th>{currentSet}</th>
            </tr> : <tr/>
          }
          </thead>
          <tbody>
            {uniqueValues
              .slice() // Create a shallow copy to avoid mutating the original array
              .sort((a, b) => {
                if (!isNaN(a) && !isNaN(b)) {
                  return Number(a) - Number(b); // Numeric sort
                }
                return String(a).localeCompare(String(b)); // Alphabetical sort
              })
              .map((value, index) => {
                const match = solutions.find((sol) => sol.values[level] === value);
                
                return (
                  <tr key={index}>
                    <td
                      onClick={() => handleCellClick(match)}
                      className="clickable-cell"
                    >
                      {value}
                    </td>
                  </tr>
                );
              })}
          </tbody>

        </table>
      );
    }

    return (
      <table className="solution-table">
        <thead>
          <tr>
            <th>{nextSet} \ {currentSet}</th> 
            {nextSet &&
              [
                ...new Set(
                  solutions.map((sol) => sol.values[level + 1])
                ),
              ].map((col, index) => <th key={index}>{col}</th>)}
          </tr>
        </thead>
        <tbody>
          {uniqueValues.map((rowValue, rowIndex) => (
            <tr key={rowIndex}>
              <td className="row-header">{rowValue}</td>
              {nextSet
                ? [
                    ...new Set(
                      solutions.map((sol) => sol.values[level + 1])
                    ),
                  ].map((colValue, colIndex) => {
                    return (
                      <td key={colIndex}>
                        {generateTable(level + 2, { ...parentFilters, [currentSet]: rowValue, [nextSet] : colValue })}
                      </td>
                    );
                  })
                : null}
            </tr>
          ))}
        </tbody>
      </table>
    );
  };

  return <div className="solution-table-container">{generateTable(0)}</div>;
};

export default SuperTable;
