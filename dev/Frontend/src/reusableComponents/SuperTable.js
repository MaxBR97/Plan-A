import React, { useState, useEffect } from "react";
import "./SuperTable.css";

const SuperTable = ({ 
  solutions, 
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

  if (displayStructure.length < 1) {
    return <p>Set structure must have at least 1 dimension.</p>;
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
   */
  function findMatchingOriginalSolutions(filters, displayedSolutions) {
    // Find displayed solutions matching the filters
    const matchingSolutions = filterByParentValues(displayedSolutions, filters);
    
    // Find corresponding original solutions
    return solutions.filter(originalSol => 
      matchingSolutions.some(displaySol => 
        // We compare with originalValues we stored earlier
        JSON.stringify(originalSol.values) === JSON.stringify(displaySol.originalValues)
      )
    );
  }

  /**
   * Check if a solution is selected
   */
  function isSolutionSelected(displaySol) {
    // Find corresponding original solutions
    const originalSol = solutions.find(sol => 
      JSON.stringify(sol.values) === JSON.stringify(displaySol.originalValues)
    );
    
    if (!originalSol) return false;
    
    // Check if it's in selectedTuples
    return selectedTuples.some(selected => 
      JSON.stringify(selected.values) === JSON.stringify(originalSol.values)
    );
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
      selectedTuples.some(selected => 
        JSON.stringify(selected.values) === JSON.stringify(original.values)
      )
    );
    
    let newSelection;
    if (allSelected) {
      // Remove from selection
      newSelection = selectedTuples.filter(selected => 
        !matchingOriginals.some(original => 
          JSON.stringify(selected.values) === JSON.stringify(original.values)
        )
      );
    } else {
      // Add to selection
      newSelection = [...selectedTuples];
      matchingOriginals.forEach(original => {
        if (!newSelection.some(selected => 
          JSON.stringify(selected.values) === JSON.stringify(original.values)
        )) {
          newSelection.push(original);
        }
      });
    }
    
    if (onSelectedTuplesChange) {
      onSelectedTuplesChange(newSelection);
    }
  };

  /**
   * Toggle selection for entire table
   */
  const toggleEntireTable = () => {
    // Check if all solutions are selected
    const allSelected = solutions.every(sol => 
      selectedTuples.some(selected => 
        JSON.stringify(selected.values) === JSON.stringify(sol.values)
      )
    );
    
    if (allSelected) {
      // Clear selection
      if (onSelectedTuplesChange) onSelectedTuplesChange([]);
    } else {
      // Select all
      if (onSelectedTuplesChange) onSelectedTuplesChange([...solutions]);
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
      const isMatch = matchingOriginals.some(original => 
        JSON.stringify(original.values) === JSON.stringify(sol.values)
      );
      
      if (isMatch) {
        const newValues = [...sol.values];
        newValues[indexInSetStructure] = editValue;
        return { ...sol, values: newValues };
      }
      return sol;
    });
    
    // Clear editing state
    setEditingCell(null);
    setEditValue("");
    
    // Notify parent component
    if (onSolutionUpdate) {
      onSolutionUpdate(updatedSolutions);
    }
  };

  /**
   * Handle adding a new item
   */
  const handleAddItem = (level, parentFilters = {}) => {
    const newDimName = `New_Dim_${dimensionCounter}`;
    setDimensionCounter(prev => prev + 1);
    
    if (level < displayStructure.length) {
      // Adding a new value to an existing dimension
      const dimensionKey = displayStructure[level];
      const indexInSetStructure = setStructure.indexOf(dimensionKey);
      
      if (indexInSetStructure === -1) return; // Dimension not found
      
      // Create a new solution
      const newSolution = {
        values: Array(setStructure.length).fill("") // Default empty values
      };
      
      // Fill in values from parent filters
      Object.entries(parentFilters).forEach(([key, val]) => {
        const idx = setStructure.indexOf(key);
        if (idx !== -1) {
          newSolution.values[idx] = val;
        }
      });
      
      // Set the new dimension value
      newSolution.values[indexInSetStructure] = newDimName;
      
      // Add to solutions
      const updatedSolutions = [...solutions, newSolution];
      
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

  // Transform solutions for display
  const displayedSolutions = transformSolutionsForDisplay(solutions);
  
  // Sort the displayed solutions
  const sortedSolutions = sortByDisplayStructure(displayedSolutions);
  console.log("selected:",selectedTuples)
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
                      if (editMode) {
                        setEditingCell({ level, filters: currentFilters });
                        setEditValue(value);
                      }
                    }}
                    className={`clickable-cell ${isSelected ? "highlighted" : ""}`}
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
              <th>{currentDimension}</th>
              <th>
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
                      if (editMode) {
                        setEditingCell({ level, filters: currentFilters });
                        setEditValue(rowValue);
                      }
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
                                  if (editMode) {
                                    setEditingCell({ level: level + 1, filters: cellFilters });
                                    setEditValue(colValue);
                                  }
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
      <table className="solution-table">
        <thead>
          <tr>
            <th 
              className="table-corner clickable-cell"
              onClick={() => toggleEntireTable()}
            >
              {currentDimension} \ {nextDimension}
            </th>
            {nextUniqueValues.map((value, index) => (
              <th 
                key={index}
                className="clickable-cell"
                onClick={() => toggleColumnSelection(value, level + 1)}
              >
                {value}
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
                onClick={() => toggleSelection({ ...parentFilters, [currentDimension]: rowValue })}
                onDoubleClick={(e) => {
                  e.stopPropagation();
                  if (editMode) {
                    setEditingCell({ level, filters: { ...parentFilters, [currentDimension]: rowValue } });
                    setEditValue(rowValue);
                  }
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
              {/* Add empty cells to match columns */}
              {Array(nextUniqueValues.length + (editMode ? 1 : 0)).fill(0).map((_, i) => (
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
      {generateTable(0)}
    </div>
  );
};

export default SuperTable;