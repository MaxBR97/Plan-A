import React, { useState } from "react";

const SuperTable = ({ solutions, setStructure, displayStructure, isDisplayBinary , valueSetName }) => {
  const [editingCell, setEditingCell] = useState(null); // Track which cell is being edited
  const [editValue, setEditValue] = useState(""); // Temporary value for editing

  if (displayStructure.length < 1) {
    return <p>Set structure must have at least 1 dimension.</p>;
  }

  const handleCellClick = (solutionTuple) => {
    //console.log(solutionTuple)
    setEditingCell(solutionTuple); // Store the full tuple being edited
    setEditValue(solutionTuple.objectiveValue); // Set current value for editing
  };

  const getRelevantValue = (sol, level) => {
        if(displayStructure[level] == valueSetName){
          return sol.objectiveValue
        }
        const lookAtIndex = setStructure.indexOf(displayStructure[level]);
        return sol.values[lookAtIndex];
  }
  

  const generateTable = (level, parentFilters = {}) => {
    // console.log(displayStructure)
    const currentSet = displayStructure[level];
    const nextSet = displayStructure[level + 1];

    const uniqueValues = [
      ...new Set(
        solutions
          .filter((sol) => {
            const x = Object.entries(parentFilters).every(([key, val]) => {
              const keyIndex = displayStructure.indexOf(key);
              return keyIndex !== -1 && getRelevantValue(sol, keyIndex) === val;
            });
            return x;
          })
          .map((sol) => getRelevantValue(sol, level))
      )
    ].sort((a, b) => {
      // Convert to numbers first, then compare
      const numA = Number(a);
      const numB = Number(b);
      
      // Check if both are valid numbers
      if (!isNaN(numA) && !isNaN(numB)) {
        return numA - numB; // Sort numbers in ascending order
      }
      
      // If not numbers, sort as strings
      return String(a).localeCompare(String(b));
    });
   

    // if(level == displayStructure.length) {
      
    //   return (
    //     <div>
    //       {uniqueValues.map((rowValue, rowIndex)=> {
    //        console.log(rowValue)
    //        return <p>{rowValue}</p>
    //       }
    //         )
    //       }
    //     </div>
    //   );
    // }

    if (level === displayStructure.length - 1) {
      return (
        <table className="solution-table">
          <thead>
          { 
            displayStructure.length == 1 ?
            <tr>
              <th>{currentSet}</th>
            </tr> : <tr></tr>
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
                const match = solutions.find((sol) => getRelevantValue(sol, level) === value);
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
    
    if (level === displayStructure.length - 2) {
      return (
        <table className="solution-table">
          <thead>
            <tr>
              <th>{currentSet}</th> 
              <th>{nextSet}</th> 
            </tr>
          </thead>
          <tbody>
            {uniqueValues.map((rowValue, rowIndex) => (
              <tr key={rowIndex}>
                <td className="row-header">{rowValue}</td>
                {nextSet
                  ?   
                     
                      generateTable(level + 1, { ...parentFilters, [currentSet]: rowValue})
                    
                      
                    
                  : null}
              </tr>
            ))}
          </tbody>
        </table>
      );
    }

    const nextUniqueValues = [
      ...new Set(
        solutions.map((sol) => getRelevantValue(sol, level+1))
      ),
    ].sort((a, b) => {
      // Convert to numbers first, then compare
      const numA = Number(a);
      const numB = Number(b);
      
      // Check if both are valid numbers
      if (!isNaN(numA) && !isNaN(numB)) {
        return numA - numB; // Sort numbers in ascending order
      }
      
      // If not numbers, sort as strings
      return String(a).localeCompare(String(b));
    });

    return (
     
      <table className="solution-table">
        <thead>
          <tr>
            <th>{nextSet} \ {currentSet}</th> 
            {nextSet &&
              nextUniqueValues.map((col, index) => <th key={index}>{col}</th>)}
          </tr>
        </thead>
        <tbody>
          {uniqueValues.map((rowValue, rowIndex) => (
            <tr key={rowIndex}>
              <td className="row-header">{rowValue}</td>
              {nextSet
                ? nextUniqueValues.map((colValue, colIndex) => {
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
