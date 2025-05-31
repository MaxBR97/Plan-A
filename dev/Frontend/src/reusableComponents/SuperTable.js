import React, { useState, useEffect } from "react";
import "./SuperTable.css";

const SuperTable = ({ 
  solutions = [], 
  setStructure, 
  displayStructure, 
  isDisplayBinary, 
  valueSetName, 
  editMode, 
  onSolutionUpdate, 
  onAddDimension,
  selectedTuples,
  onSelectedTuplesChange,
  defaultObjectiveValue = 0
}) => {
  const [editingCell, setEditingCell] = useState(null); // Track which cell is being edited
  const [editValue, setEditValue] = useState(""); // Temporary value for editing
  const [dimensionCounter, setDimensionCounter] = useState(1); // For generating unique dimension names
  useEffect(() => {
    // Reset editing when solutions change
    setEditingCell(null);
    setEditValue("");
  }, [solutions]);

  // Return empty state message if no display structure
  if (!displayStructure || displayStructure.length === 0) {
    return (
      <div className="empty-table-message">
        No dimensions available to display
      </div>
    );
  }

  /**
   * Handle adding a new item
   */
  const handleAddItem = (level, parentFilters = {}) => {
    const newDimName = `New_Val_${dimensionCounter}`;
    setDimensionCounter(prev => prev + 1);
    
    if (level < displayStructure.length) {
      // Adding a new value to an existing dimension
      const dimensionKey = displayStructure[level];
      const indexInSetStructure = setStructure.indexOf(dimensionKey);
      
      if (indexInSetStructure === -1) return; // Dimension not found

      // Get all unique values for each dimension
      const dimensionValues = {};
      displayStructure.forEach((dim, idx) => {
        // When adding a row (level === 0), collect values only up to the current nesting level
        // When adding a column/nested value, only collect up to current level
        if ((level === 0 && idx <= 1) || (level > 0 && idx <= level)) {
          const values = new Set(solutions.map(sol => {
            const dimIndex = setStructure.indexOf(dim);
            return dimIndex !== -1 ? sol.values[dimIndex] : "";
          }));
          dimensionValues[dim] = Array.from(values).filter(v => v !== "");
        }
      });

      const newSolutions = [];
      
      // If we're adding a column and there are existing rows
      if (level > 0 && dimensionValues[displayStructure[0]]?.length > 0) {
        dimensionValues[displayStructure[0]].forEach(rowValue => {
          const baseValues = Array(setStructure.length).fill("");
          const rowIndex = setStructure.indexOf(displayStructure[0]);
          baseValues[rowIndex] = rowValue;
          
          // Fill in parent filter values
          Object.entries(parentFilters).forEach(([key, val]) => {
            const idx = setStructure.indexOf(key);
            if (idx !== -1) {
              baseValues[idx] = val;
            }
          });
          
          // Set the new dimension value
          baseValues[indexInSetStructure] = newDimName;
          
          newSolutions.push({
            values: baseValues,
            objectiveValue: defaultObjectiveValue
          });
        });
      } else {
        // If adding a row (level === 0)
        // Special case for 2D tables
        if (displayStructure.length === 2) {
          // Create a single row with empty second dimension
          const baseValues = Array(setStructure.length).fill("");
          baseValues[indexInSetStructure] = newDimName;
          
          // Fill in parent filter values
          Object.entries(parentFilters).forEach(([key, val]) => {
            const idx = setStructure.indexOf(key);
            if (idx !== -1) {
              baseValues[idx] = val;
            }
          });
          
          newSolutions.push({
            values: baseValues,
            objectiveValue: defaultObjectiveValue
          });
        } else {
          // For higher dimension tables, get values for the immediate next level (columns)
          const columnValues = dimensionValues[displayStructure[1]] || [];
          
          if (columnValues.length > 0) {
            // Create a new solution for each column value
            columnValues.forEach(columnValue => {
              const baseValues = Array(setStructure.length).fill("");
              
              // Set the new row value
              baseValues[indexInSetStructure] = newDimName;
              
              // Set the column value
              const columnIndex = setStructure.indexOf(displayStructure[1]);
              if (columnIndex !== -1) {
                baseValues[columnIndex] = columnValue;
              }
              
              // Fill in parent filter values
              Object.entries(parentFilters).forEach(([key, val]) => {
                const idx = setStructure.indexOf(key);
                if (idx !== -1) {
                  baseValues[idx] = val;
                }
              });
              
              // All deeper nested values remain as ""
              newSolutions.push({
                values: baseValues,
                objectiveValue: defaultObjectiveValue
              });
            });
          } else {
            // If no column values exist, create a single empty entry
            const baseValues = Array(setStructure.length).fill("");
            baseValues[indexInSetStructure] = newDimName;
            
            // Fill in parent filter values
            Object.entries(parentFilters).forEach(([key, val]) => {
              const idx = setStructure.indexOf(key);
              if (idx !== -1) {
                baseValues[idx] = val;
              }
            });
            
            newSolutions.push({
              values: baseValues,
              objectiveValue: defaultObjectiveValue
            });
          }
        }
      }

      // Add to solutions
      const updatedSolutions = [...solutions, ...newSolutions];
      
      if (onSolutionUpdate) {
        onSolutionUpdate(updatedSolutions);
      }
    } else {
      // Adding a new dimension
      if (onAddDimension) {
        onAddDimension(newDimName);
      }
    }
  };

  // If no solutions but we have display structure, show empty editable table
  if (solutions.length === 0) {
    return (
      <div className="super-table-container">
        <div className="table-scrollable">
          <table className="solution-table">
            <thead>
              <tr>
                {displayStructure.map((dimension, index) => (
                  <th key={index}>{dimension}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {editMode && (
                <tr>
                  <td 
                    className="add-item-cell"
                    onClick={() => handleAddItem(0)}
                    colSpan={displayStructure.length}
                  >
                    <span className="add-button">+</span>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  /**
   * Transforms the solutions to match the display structure
   * This is the critical function we're fixing
   */
  function transformSolutionsForDisplay(solutionsData) {
    // Create a clean copy of our solutions
    let transformed = JSON.parse(JSON.stringify(solutionsData));
    
    // For each solution, create a new values array based on displayStructure
    return transformed.map(sol => {
      const originalValues = [...sol.values]; // Keep the original values
      const displayValues = [];
      
      // For each dimension in displayStructure, find its value in the solution
      displayStructure.forEach(dimension => {
        const indexInSet = setStructure.indexOf(dimension);
        // If dimension exists in setStructure, use its value; otherwise use a default value
        if (indexInSet !== -1) {
          displayValues.push(originalValues[indexInSet]);
        } else {
          displayValues.push(""); // Default empty value for unknown dimensions
        }
      });
      
      return {
        values: displayValues,
        originalValues // Keep a reference to original values for selection matching
      };
    });
  }

  /**
   * Sorts an array of objects (`data`) by the order of keys in `displayStructure`.
   */
  function sortByDisplayStructure(data) {
    return [...data].sort((a, b) => {
      for (let i = 0; i < a.values.length; i++) {
        const valA = a.values[i];
        const valB = b.values[i];

        const numA = Number(valA);
        const numB = Number(valB);

        const bothNumbers = !isNaN(numA) && !isNaN(numB);

        if (bothNumbers && numA !== numB) return numA - numB;
        if (!bothNumbers && String(valA) !== String(valB)) {
          return String(valA).localeCompare(String(valB));
        }
      }
      return 0;
    });
  }

  /**
   * Filters solutions based on parent filter values
   */
  function filterByParentValues(solutions, parentFilters) {
    return solutions.filter(sol => {
      return Object.entries(parentFilters).every(([key, value]) => {
        const indexInDisplay = displayStructure.indexOf(key);
        return indexInDisplay >= 0 && sol.values[indexInDisplay] === value;
      });
    });
  }

  /**
   * Gets all values at a particular index from an array of solutions
   */
  function getValuesAtIndex(solutions, index) {
    return solutions.map(sol => sol.values[index]);
  }

  /**
   * Sorts an array of values, numerically when possible
   */
  function sortMixedValues(values) {
    return [...values].sort((a, b) => {
      const numA = Number(a);
      const numB = Number(b);

      const bothNumbers = !isNaN(numA) && !isNaN(numB);

      if (bothNumbers) return numA - numB;
      return String(a).localeCompare(String(b));
    });
  }

  /**
 * Find matching tuples with original solutions
 * FIX: Correct dimension alignment between displayStructure and setStructure
 */
function findMatchingOriginalSolutions(filters, displayedSolutions) {
  // Find displayed solutions matching the filters
  const matchingDisplayed = filterByParentValues(displayedSolutions, filters);

  return solutions.filter(originalSol => 
    matchingDisplayed.some(displaySol => {
      // Build dimension -> value maps for both original and display solutions
      const originalMap = {};
      setStructure.forEach((dim, i) => {
        if (dim !== valueSetName) {
          originalMap[dim] = originalSol.values[i];
        }
      });

      const displayMap = {};
      displayStructure.forEach((dim, i) => {
        if (dim !== valueSetName) {
          displayMap[dim] = displaySol.values[i];
        }
      });

      // Compare all relevant dimensions in displayStructure
      return displayStructure.every(dim => {
        if (dim === valueSetName) return true; // Skip value comparison
        return originalMap[dim] === displayMap[dim];
      });
    })
  );
}


  function isSolutionSelected(displaySol) {
    // Build a map: dimension -> value from displaySol (in displayStructure order)
    const dimToValueMap = {};
    displayStructure.forEach((dim, i) => {
      dimToValueMap[dim] = displaySol.values[i];
    });
  
    // Get the dimensions we're going to match on (excluding valueSetName)
    const matchDims = displayStructure.filter(dim => dim !== valueSetName);
  
    return selectedTuples.some(selected => {
      // Rebuild selected dim -> value based on setStructure
      const selectedDimToValue = {};
      setStructure.forEach((dim, i) => {
        selectedDimToValue[dim] = selected.values[i];
      });
  
      // Only compare relevant dimensions
      return matchDims.every(dim => selectedDimToValue[dim] === dimToValueMap[dim]);
    });
  }
  
  /**
 * Extract tuple values in the order of displayStructure (excluding valueSetName)
 */
function extractValuesInDisplayOrder(tuple) {
  const dimToVal = {};
  setStructure.forEach((dim, i) => {
    dimToVal[dim] = tuple.values[i];
  });

  return displayStructure
    .filter(dim => dim !== valueSetName)
    .map(dim => dimToVal[dim]);
}

/**
 * Toggle selection for tuples matching the filters
 */
const toggleSelection = (filters) => {
  // Find displayed solutions matching the filters
  const matchingDisplayed = filterByParentValues(displayedSolutions, filters);
  if (matchingDisplayed.length === 0) return;

  // Find corresponding original solutions
  const matchingOriginals = findMatchingOriginalSolutions(filters, displayedSolutions);
  // Check if they're all selected already
  const allSelected = matchingOriginals.every(original =>
    selectedTuples.some(selected => {
      const originalValues = extractValuesInDisplayOrder(original);
      const selectedValues = extractValuesInDisplayOrder(selected);
      return JSON.stringify(originalValues) === JSON.stringify(selectedValues);
    })
  );

  let newSelection;
  if (allSelected) {
    // Remove from selection
    newSelection = selectedTuples.filter(selected =>
      !matchingOriginals.some(original => {
        const originalValues = extractValuesInDisplayOrder(original);
        const selectedValues = extractValuesInDisplayOrder(selected);
        return JSON.stringify(originalValues) === JSON.stringify(selectedValues);
      })
    );

  } else {
    // Add to selection
    newSelection = [...selectedTuples];
    matchingOriginals.forEach(original => {
      const alreadySelected = newSelection.some(selected => {
        const originalValues = extractValuesInDisplayOrder(original);
        const selectedValues = extractValuesInDisplayOrder(selected);
        return JSON.stringify(originalValues) === JSON.stringify(selectedValues);
      });

      if (!alreadySelected) {
        newSelection.push(original);
      }
    });
  }

  if (onSelectedTuplesChange) {
    onSelectedTuplesChange(newSelection);
  }
};


  /**
   * Toggle selection for entire table or subtable based on level and parent filters
   */
  const toggleEntireTable = (level, parentFilters = {}) => {
    // Get solutions that match the parent filters
    const matchingSolutions = filterByParentValues(sortedSolutions, parentFilters);
    
    // Find corresponding original solutions
    const matchingOriginals = findMatchingOriginalSolutions(parentFilters, matchingSolutions);

    // Check if all matching solutions are selected
    const allSelected = matchingOriginals.every(original =>
      selectedTuples.some(selected => {
        const originalValues = extractValuesInDisplayOrder(original);
        const selectedValues = extractValuesInDisplayOrder(selected);
        return JSON.stringify(originalValues) === JSON.stringify(selectedValues);
      })
    );

    let newSelection;
    if (allSelected) {
      // Remove all matching solutions from selection
      newSelection = selectedTuples.filter(selected =>
        !matchingOriginals.some(original => {
          const originalValues = extractValuesInDisplayOrder(original);
          const selectedValues = extractValuesInDisplayOrder(selected);
          return JSON.stringify(originalValues) === JSON.stringify(selectedValues);
        })
      );
    } else {
      // Add all matching solutions to selection
      newSelection = [...selectedTuples];
      matchingOriginals.forEach(original => {
        const alreadySelected = newSelection.some(selected => {
          const originalValues = extractValuesInDisplayOrder(original);
          const selectedValues = extractValuesInDisplayOrder(selected);
          return JSON.stringify(originalValues) === JSON.stringify(selectedValues);
        });

        if (!alreadySelected) {
          newSelection.push(original);
        }
      });
    }

    if (onSelectedTuplesChange) {
      onSelectedTuplesChange(newSelection);
    }
  };

  /**
   * Toggle selection for a column
   */
  const toggleColumnSelection = (columnValue, level) => {
    const columnDimension = displayStructure[level];
    const filters = { [columnDimension]: columnValue };
    
    // Use the same logic as toggleSelection
    toggleSelection(filters);
  };

  /**
 * Apply the edited value
 * FIX: Ensure we're editing the correct dimension and update selected tuples
 */
const applyEdit = () => {
  if (!editingCell) return;
  const { level, filters } = editingCell;
  
  // Get the dimension being edited
  const editingDimension = displayStructure[level];
  const indexInSetStructure = setStructure.indexOf(editingDimension);
  if (indexInSetStructure === -1) return; // Dimension not found in set structure
  
  // Find matching displayed solutions
  const matchingDisplayed = filterByParentValues(displayedSolutions, filters);
  
  // Find corresponding original solutions
  const matchingOriginals = findMatchingOriginalSolutions(filters, displayedSolutions);
  
  // Update all matching solutions
  const updatedSolutions = solutions.map(sol => {
    const isMatch = matchingOriginals.some(original => {
      // Compare only non-value parts
      const originalValues = original.values.filter((_, i) => 
        displayStructure.includes(setStructure[i]) && setStructure[i] !== valueSetName
      );
      
      const solValues = sol.values.filter((_, i) => 
        displayStructure.includes(setStructure[i]) && setStructure[i] !== valueSetName
      );
      
      const minLength = Math.min(originalValues.length, solValues.length);
      return JSON.stringify(originalValues.slice(0, minLength)) === 
             JSON.stringify(solValues.slice(0, minLength));
    });
    
    if (isMatch) {
      const newValues = [...sol.values];
      newValues[indexInSetStructure] = editValue;
      return { ...sol, values: newValues.slice(0,displayStructure.length) };
    }
    
    return { ...sol, values: sol.values.slice(0,displayStructure.length) };
  });
  
  // Also update any matching tuples in the selectedTuples array
  if (selectedTuples && onSelectedTuplesChange) {
    const updatedSelectedTuples = selectedTuples.map(selectedTuple => {
      const isMatch = matchingOriginals.some(original => {
        // Compare only non-value parts
        const originalValues = original.values.filter((_, i) => 
          displayStructure.includes(setStructure[i]) && setStructure[i] !== valueSetName
        );
        
        const selectedValues = selectedTuple.values.filter((_, i) => 
          displayStructure.includes(setStructure[i]) && setStructure[i] !== valueSetName
        );
        
        const minLength = Math.min(originalValues.length, selectedValues.length);
        return JSON.stringify(originalValues.slice(0, minLength)) === 
               JSON.stringify(selectedValues.slice(0, minLength));
      });
      
      if (isMatch) {
        const newValues = [...selectedTuple.values];
        newValues[indexInSetStructure] = editValue;
        return { ...selectedTuple, values: newValues.slice(0, displayStructure.length) };
      }
      
      return selectedTuple;
    });
    
    // Update the selected tuples
    onSelectedTuplesChange(updatedSelectedTuples);
  }
  
  // Clear editing state
  setEditingCell(null);
  setEditValue("");
  
  // Notify parent component
  if (onSolutionUpdate) {
    onSolutionUpdate(updatedSolutions);
  }
};

  

  // Transform solutions for display
  const displayedSolutions = transformSolutionsForDisplay(solutions);
  
  // Sort the displayed solutions
  const sortedSolutions = sortByDisplayStructure(displayedSolutions);
  const generateTable = (level, parentFilters = {}) => {
    const currentDimension = displayStructure[level];
    const nextDimension = displayStructure[level + 1];
    const filteredSolutions = filterByParentValues(sortedSolutions, parentFilters);
    const currentValues = getValuesAtIndex(filteredSolutions, level);
    const uniqueValues = sortMixedValues([...new Set(currentValues)]);

    // For the leaf level (most detailed)
    if (level === displayStructure.length - 1) {
      return (
        <table className="solution-table">
          <thead>
            {displayStructure.length === 1 ? (
              <tr>
                <th>{currentDimension}</th>
              </tr>
            ) : (
              <tr></tr>
            )}
          </thead>
          <tbody>
            {uniqueValues.map((value, index) => {
              const currentFilters = { ...parentFilters, [currentDimension]: value };
              const matchingSolutions = filterByParentValues(sortedSolutions, currentFilters);
              const isSelected = matchingSolutions.some(sol => isSolutionSelected(sol));
              
              return (
                <tr key={index}>
                  <td
                    onClick={() => toggleSelection(currentFilters)}
                    onDoubleClick={(e) => {
                      e.stopPropagation();
                      setEditingCell({ level, filters: currentFilters });
                      setEditValue(value);
                    }}
                    className={`row-header clickable-cell ${isSelected ? "highlighted" : ""}`}
                  >
                    {editingCell &&
                    editingCell.level === level &&
                    JSON.stringify(editingCell.filters) === JSON.stringify(currentFilters) ? (
                      <input
                        autoFocus
                        value={editValue}
                        onChange={(e) => setEditValue(e.target.value)}
                        onBlur={applyEdit}
                        onKeyDown={(e) => {
                          if (e.key === "Enter") applyEdit();
                        }}
                        onClick={(e) => e.stopPropagation()}
                      />
                    ) : (
                      value
                    )}
                  </td>
                </tr>
              );
            })}
            {/* Add new row button in edit mode */}
            {editMode && (
              <tr>
                <td 
                  className="add-item-cell"
                  onClick={() => handleAddItem(level, parentFilters)}
                >
                  <span className="add-button">+</span>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      );
    }
    
    // For the second-to-last level
    if (level === displayStructure.length - 2) {
      return (
        <table className="solution-table">
          <thead>
            <tr>
              <th 
                className="table-corner clickable-cell"
                onClick={() => toggleEntireTable(level, parentFilters)}
              >
                {currentDimension}
              </th>
              <th 
                className="table-corner clickable-cell"
                onClick={() => toggleEntireTable(level, parentFilters)}
              >
                {nextDimension}
              </th>
            </tr>
          </thead>
          <tbody>
            {uniqueValues.map((rowValue, rowIndex) => {
              const currentFilters = { ...parentFilters, [currentDimension]: rowValue };
              const matchingSolutions = filterByParentValues(sortedSolutions, currentFilters);
              const isSelected = matchingSolutions.some(sol => isSolutionSelected(sol));
              
              // For generating the cell content (based on nextDimension)
              const nextValues = getValuesAtIndex(matchingSolutions, level + 1);
              const nextUniqueValues = sortMixedValues([...new Set(nextValues)]);
              
              return (
                <tr key={rowIndex}>
                  <td 
                    className={`row-header clickable-cell ${isSelected ? "highlighted" : ""}`}
                    onClick={() => toggleSelection(currentFilters)}
                    onDoubleClick={(e) => {
                      e.stopPropagation();
                      setEditingCell({ level, filters: currentFilters });
                      setEditValue(rowValue);
                    }}
                  >
                    {editingCell &&
                    editingCell.level === level &&
                    JSON.stringify(editingCell.filters) === JSON.stringify(currentFilters) ? (
                      <input
                        autoFocus
                        value={editValue}
                        onChange={(e) => setEditValue(e.target.value)}
                        onBlur={applyEdit}
                        onKeyDown={(e) => {
                          if (e.key === "Enter") applyEdit();
                        }}
                        onClick={(e) => e.stopPropagation()}
                      />
                    ) : (
                      rowValue
                    )}
                  </td>
                  <td>
                    <table className="inner-table">
                      <tbody>
                        {nextUniqueValues.map((colValue, colIndex) => {
                          const cellFilters = { 
                            ...parentFilters, 
                            [currentDimension]: rowValue, 
                            [nextDimension]: colValue 
                          };
                          const cellMatchingSolutions = filterByParentValues(sortedSolutions, cellFilters);
                          const isCellSelected = cellMatchingSolutions.some(sol => 
                            isSolutionSelected(sol)
                          );
                          
                          return (
                            <tr key={colIndex}>
                              <td 
                                className={`clickable-cell ${isCellSelected ? "highlighted" : ""}`}
                                onClick={() => toggleSelection(cellFilters)}
                                onDoubleClick={(e) => {
                                  e.stopPropagation();
                                  // if (editMode) {
                                    setEditingCell({ level: level + 1, filters: cellFilters });
                                    setEditValue(colValue);
                                  // }
                                }}
                              >
                                {editingCell &&
                                editingCell.level === level + 1 &&
                                JSON.stringify(editingCell.filters) === JSON.stringify(cellFilters) ? (
                                  <input
                                    autoFocus
                                    value={editValue}
                                    onChange={(e) => setEditValue(e.target.value)}
                                    onBlur={applyEdit}
                                    onKeyDown={(e) => {
                                      if (e.key === "Enter") applyEdit();
                                    }}
                                    onClick={(e) => e.stopPropagation()}
                                  />
                                ) : (
                                  colValue
                                )}
                              </td>
                            </tr>
                          );
                        })}
                        {/* Add new item button in edit mode */}
                        {editMode && (
                          <tr>
                            <td 
                              className="add-item-cell"
                              onClick={() => handleAddItem(level + 1, {
                                ...parentFilters, 
                                [currentDimension]: rowValue
                              })}
                            >
                              <span className="add-button">+</span>
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </td>
                </tr>
              );
            })}
            {/* Add new row button in edit mode */}
            {editMode && (
              <tr>
                <td 
                  className="add-item-cell"
                  onClick={() => handleAddItem(level, parentFilters)}
                >
                  <span className="add-button">+</span>
                </td>
                <td></td>
              </tr>
            )}
          </tbody>
        </table>
      );
    }

    // For upper levels (with nested tables)
    const nextValues = getValuesAtIndex(filteredSolutions, level + 1);
    const nextUniqueValues = sortMixedValues([...new Set(nextValues)]);

    return (
      <table className={`solution-table level-${level}`}>
        <thead>
          <tr>
            <th 
              className="table-corner clickable-cell"
              onClick={() => toggleEntireTable(level, parentFilters)}
            >
              {currentDimension} \ {nextDimension}
            </th>
            {nextUniqueValues.map((value, index) => (
              <th 
                key={index}
                className="clickable-cell"
                onClick={() => toggleColumnSelection(value, level + 1)}
                onDoubleClick={(e) => {  // FIX: Add double-click handler for column headers
                  e.stopPropagation();
                  setEditingCell({ level: level + 1, filters: { [nextDimension]: value } });
                  setEditValue(value);
                }}
              >
                {editingCell &&
                editingCell.level === level + 1 &&
                JSON.stringify(editingCell.filters) === JSON.stringify({ [nextDimension]: value }) ? (
                  <input
                    autoFocus
                    value={editValue}
                    onChange={(e) => setEditValue(e.target.value)}
                    onBlur={applyEdit}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") applyEdit();
                    }}
                    onClick={(e) => e.stopPropagation()}
                  />
                ) : (
                  value
                )}
              </th>
            ))}
            {/* Add column button for upper levels */}
            {editMode && (
              <th 
                className="add-item-cell"
                onClick={() => handleAddItem(level + 1, parentFilters)}
              >
                <span className="add-button">+</span>
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {uniqueValues.map((rowValue, rowIndex) => (
            <tr key={rowIndex}>
              <td 
                className="row-header clickable-cell"
                onClick={() => {toggleSelection({ ...parentFilters, [currentDimension]: rowValue })}}
                onDoubleClick={(e) => {
                  e.stopPropagation();
                  // if (editMode) {
                    setEditingCell({ level, filters: { ...parentFilters, [currentDimension]: rowValue } });
                    
                    setEditValue(rowValue);
                  // }
                }}
              >
                {editingCell &&
                editingCell.level === level &&
                JSON.stringify(editingCell.filters) === JSON.stringify({ ...parentFilters, [currentDimension]: rowValue }) ? (
                  <input
                    autoFocus
                    value={editValue}
                    onChange={(e) => setEditValue(e.target.value)}
                    onBlur={applyEdit}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") applyEdit();
                    }}
                    onClick={(e) => e.stopPropagation()}
                  />
                ) : (
                  rowValue
                )}
              </td>
              {nextUniqueValues.map((colValue, colIndex) => {
                const cellFilters = { 
                  ...parentFilters, 
                  [currentDimension]: rowValue, 
                  [nextDimension]: colValue 
                };
                return (
                  <td key={colIndex}>
                    {generateTable(level + 2, cellFilters)}
                  </td>
                );
              })}
              {/* Add an empty cell to match the "add column" header */}
              {editMode && <td></td>}
            </tr>
          ))}
          {/* Add new row button in edit mode */}
          {editMode && (
            <tr>
              <td 
                className="add-item-cell"
                onClick={() => handleAddItem(level, parentFilters)}
              >
                <span className="add-button">+</span>
              </td>
              {Array(nextUniqueValues.length + 1).fill(0).map((_, i) => (
                <td key={i}></td>
              ))}
            </tr>
          )}
        </tbody>
      </table>
    );
  };

  return (
    <div className="super-table-container">
      <div className="table-scrollable">
        {generateTable(0)}
      </div>
    </div>
  );
  
  
  
};

export default SuperTable;