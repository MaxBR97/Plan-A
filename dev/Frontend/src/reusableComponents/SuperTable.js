import React, { useState } from "react";
import "./SuperTable.css";

const SuperTable = ({ solutions, setStructure, displayStructure, isDisplayBinary, valueSetName }) => {
  const [editingCell, setEditingCell] = useState(null); // Track which cell is being edited
  const [editValue, setEditValue] = useState(""); // Temporary value for editing
  const [selectedTuples, setSelectedTuples] = useState([]);

  if (displayStructure.length < 1) {
    return <p>Set structure must have at least 1 dimension.</p>;
  }

  /**
   * Sorts an array of objects (`data`) by the order of keys in `displayStructure`.
   * Only keys in `displayStructure` affect sorting.
   */
  function sortByKeyStructure(data, displayStructure) {
    return [...data].sort((a, b) => {
      for (const key of displayStructure) {
        const valA = a[key];
        const valB = b[key];

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

  function reorderObjectKeys(data, setStructure, displayStructure) {
    return data.map(obj => {
      const reorderedValues = new Array(displayStructure.length);

      displayStructure.forEach((key, newIndex) => {
        const currentIndex = setStructure.indexOf(key);
        if (currentIndex !== -1) {
          reorderedValues[newIndex] = obj.values[currentIndex];
        }
      });

      return {
        ...obj,
        values: reorderedValues
      };
    });
  }

  /**
   * Filters an array of objects based on key-value pairs in `parentFilters`.
   */
  function filterByParentFilters(data, parentFilters, structure) {
    return data.filter(obj => {
      return Object.entries(parentFilters).every(([key, val]) => {
        // Find the index in the 'values' array based on the key from the structure
        const index = structure.indexOf(key);
        if (index === -1) {
          // If the key doesn't exist in the structure, ignore the filter
          return true;
        }
        // Compare the value at the corresponding index in the values array
        return obj.values[index] === val;
      });
    });
  }

  /**
   * Extracts all values from the 'values' array at a given index in each object.
   */
  function getValuesAtIndex(data, index) {
    return data.map(obj => obj.values[index]); // Extracts the value at the specified index from each 'values' array
  }

  /**
   * Sorts an array of values, numerically when possible, otherwise lexicographically.
   */
  function sortMixedValues(values) {
    if (Array.isArray(values)) {
      // Sort array as before
      return values.sort((a, b) => {
        const numA = Number(a);
        const numB = Number(b);

        const bothNumbers = !isNaN(numA) && !isNaN(numB);

        if (bothNumbers) return numA - numB;
        return String(a).localeCompare(String(b));
      });
    } else if (typeof values === 'object' && values !== null) {
      // Sort object keys by their corresponding values
      const sortedEntries = Object.entries(values).sort(([, valueA], [, valueB]) => {
        const numA = Number(valueA);
        const numB = Number(valueB);

        const bothNumbers = !isNaN(numA) && !isNaN(numB);

        if (bothNumbers) return numA - numB;
        return String(valueA).localeCompare(String(valueB));
      });

      // Return a new object with sorted keys and values
      return Object.fromEntries(sortedEntries);
    }

    // Return the input as is if it's neither an array nor an object
    return values;
  }

  /**
   * Find all tuples that match the given filters
   */
  const findMatchingTuples = (filters) => {
    return solutions.filter(sol => {
      return Object.entries(filters).every(([key, val]) => {
        const idx = displayStructure.indexOf(key);
        return getRelevantValue(sol, idx) === val;
      });
    });
  };

  /**
   * Toggle selection for multiple tuples 
   */
  const toggleSelection = (filters) => {
    const matchedTuples = findMatchingTuples(filters);
    
    // Create a map of selected tuples for easy lookup
    const selectedMap = new Map(selectedTuples.map(tuple => [JSON.stringify(tuple), tuple]));
    
    // Determine if we're adding or removing
    const firstMatchIdx = selectedTuples.findIndex(s => JSON.stringify(s) === JSON.stringify(matchedTuples[0]));
    const isSelecting = firstMatchIdx === -1;
    
    if (isSelecting) {
      // Add all matched tuples that aren't already selected
      matchedTuples.forEach(tuple => {
        const tupleKey = JSON.stringify(tuple);
        if (!selectedMap.has(tupleKey)) {
          selectedMap.set(tupleKey, tuple);
        }
      });
    } else {
      // Remove all matched tuples
      matchedTuples.forEach(tuple => {
        const tupleKey = JSON.stringify(tuple);
        selectedMap.delete(tupleKey);
      });
    }
    
    setSelectedTuples(Array.from(selectedMap.values()));
  };

  /**
   * Check if a specific solution is highlighted
   */
  const isHighlighted = (sol) => {
    return selectedTuples.some(s => JSON.stringify(s) === JSON.stringify(sol));
  };

  /**
   * Get the value at a specific level
   */
  const getRelevantValue = (sol, level) => {
    if (displayStructure[level] === valueSetName) {
      return sol.objectiveValue;
    }
    const lookAtIndex = setStructure.indexOf(displayStructure[level]);
    return sol.values[lookAtIndex];
  };

  /**
   * Get tuples filtered by parent filters
   */
  const getFilteredTuples = (solutions, parentFilters, displayStructure, setStructure) => {
    return solutions.filter((sol) => {
      return Object.entries(parentFilters).every(([key, val]) => {
        const levelIndex = displayStructure.indexOf(key);
        if (levelIndex === -1) return false; // Skip if the key is not part of displayStructure

        if (displayStructure[levelIndex] === valueSetName) {
          // valueSetName level - use objectiveValue
          return sol.objectiveValue === val;
        }

        const solValue = sol.values[setStructure.indexOf(displayStructure[levelIndex])];
        return solValue === val;
      });
    });
  };

  /**
   * Apply the edited value
   */
  const applyEdit = () => {
    if (!editingCell) return;
    const { level, filters } = editingCell;

    // Get the key being edited
    const editingKey = displayStructure[level];
    
    // Find the index of the key in setStructure
    const idxToUpdate = setStructure.indexOf(editingKey);
    if (idxToUpdate === -1 && editingKey !== valueSetName) return;

    // Create a new array of solutions
    const updatedSolutions = solutions.map(sol => {
      // Check if this solution matches the filters
      const match = Object.entries(filters).every(([key, val]) => {
        const idx = displayStructure.indexOf(key);
        return getRelevantValue(sol, idx) === val;
      });

      if (match) {
        if (editingKey === valueSetName) {
          return { ...sol, objectiveValue: editValue };
        } else {
          const newValues = [...sol.values];
          newValues[idxToUpdate] = editValue;
          return { ...sol, values: newValues };
        }
      }
      return sol;
    });

    // Clear editing state
    setEditingCell(null);
    setEditValue("");
    
    // Update solutions - assumes there's a way to update solutions in the parent component
    // This is just an example, you might need to adapt this to your actual data flow
    setStructure(updatedSolutions);
    // solutions = updatedSolutions
  };

  let mutatedSolutions = reorderObjectKeys(solutions, setStructure, displayStructure);
  mutatedSolutions = sortByKeyStructure(mutatedSolutions, displayStructure);

  const generateTable = (level, parentFilters = {}) => {
    const currentSet = displayStructure[level];
    const nextSet = displayStructure[level + 1];
    const filteredSolutions = filterByParentFilters(mutatedSolutions, parentFilters, displayStructure);
    const rawValues = getValuesAtIndex(filteredSolutions, level);
    const uniqueValues = sortMixedValues([...new Set(rawValues)]);

    // For the leaf level (most detailed)
    if (level === displayStructure.length - 1) {
      return (
        <table className="solution-table">
          <thead>
            {displayStructure.length === 1 ? (
              <tr>
                <th>{currentSet}</th>
              </tr>
            ) : (
              <tr></tr>
            )}
          </thead>
          <tbody>
            {uniqueValues.map((value, index) => {
              const currentFilters = { ...parentFilters, [currentSet]: value };
              const matchingTuples = findMatchingTuples(currentFilters);
              const isSelected = matchingTuples.some(tuple => isHighlighted(tuple));
              
              return (
                <tr key={index}>
                  <td
                    onClick={() => toggleSelection(currentFilters)}
                    onDoubleClick={(e) => {
                      e.stopPropagation();
                      setEditingCell({ level, filters: currentFilters });
                      setEditValue(value);
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
              <th>{currentSet}</th>
              <th>{nextSet}</th>
            </tr>
          </thead>
          <tbody>
            {uniqueValues.map((rowValue, rowIndex) => {
              const currentFilters = { ...parentFilters, [currentSet]: rowValue };
              const matchingTuples = findMatchingTuples(currentFilters);
              const isSelected = matchingTuples.some(tuple => isHighlighted(tuple));
              
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
                  {nextSet ? generateTable(level + 1, currentFilters) : null}
                </tr>
              );
            })}
          </tbody>
        </table>
      );
    }

    // For upper levels (with nested tables)
    const nextRawValues = getValuesAtIndex(mutatedSolutions, level + 1);
    const nextUniqueValues = sortMixedValues([...new Set(nextRawValues)]);

    return (
      <table className="solution-table">
        <thead>
          <tr>
            <th>{currentSet} \ {nextSet}</th>
            {nextSet &&
              nextUniqueValues.map((colValue, index) => {
                const colFilters = { ...parentFilters, [nextSet]: colValue };
                const matchingTuples = findMatchingTuples(colFilters);
                const isSelected = matchingTuples.some(tuple => isHighlighted(tuple));
                
                return (
                  <th 
                    key={index}
                    className={`clickable-cell ${isSelected ? "highlighted" : ""}`}
                    onClick={() => toggleSelection(colFilters)}
                    onDoubleClick={(e) => {
                      e.stopPropagation();
                      setEditingCell({ level: level + 1, filters: colFilters });
                      setEditValue(colValue);
                    }}
                  >
                    {editingCell &&
                    editingCell.level === level + 1 &&
                    JSON.stringify(editingCell.filters) === JSON.stringify(colFilters) ? (
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
                  </th>
                );
              })}
          </tr>
        </thead>
        <tbody>
          {uniqueValues.map((rowValue, rowIndex) => {
            const rowFilters = { ...parentFilters, [currentSet]: rowValue };
            const matchingTuples = findMatchingTuples(rowFilters);
            const isSelected = matchingTuples.some(tuple => isHighlighted(tuple));
            
            return (
              <tr key={rowIndex}>
                <td 
                  className={`row-header clickable-cell ${isSelected ? "highlighted" : ""}`}
                  onClick={() => toggleSelection(rowFilters)}
                  onDoubleClick={(e) => {
                    e.stopPropagation();
                    setEditingCell({ level, filters: rowFilters });
                    setEditValue(rowValue);
                  }}
                >
                  {editingCell &&
                  editingCell.level === level &&
                  JSON.stringify(editingCell.filters) === JSON.stringify(rowFilters) ? (
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
                {nextSet
                  ? nextUniqueValues.map((colValue, colIndex) => {
                      const cellFilters = { ...parentFilters, [currentSet]: rowValue, [nextSet]: colValue };
                      const matchingTuples = findMatchingTuples(cellFilters);
                      const isSelected = matchingTuples.some(tuple => isHighlighted(tuple));
                      
                      return (
                        <td 
                          key={colIndex} 
                          className={isSelected ? "highlighted-container" : ""}
                        >
                          {generateTable(level + 2, cellFilters)}
                        </td>
                      );
                    })
                  : null}
              </tr>
            );
          })}
        </tbody>
      </table>
    );
  };

  return (
    <div className="solution-table-container">
      {generateTable(0)}
      <div className="selection-info">
        <p>Selected tuples: {selectedTuples.length}</p>
      </div>
    </div>
  );
};

export default SuperTable;