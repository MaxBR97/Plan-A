import React, { useState } from "react";
import { useZPL } from "../context/ZPLContext";
import "./SolutionResultsPage.css";

const SolutionResultsPage = () => {
  const { solutionResponse } = useZPL();
  const [selectedVariable, setSelectedVariable] = useState(
    Object.keys(solutionResponse?.solution || {})[0]
  );

  if (!solutionResponse || !solutionResponse.solution) {
    return <p>No solution data available.</p>;
  }

  const handleVariableChange = (event) => {
    setSelectedVariable(event.target.value);
  };

  const variableData = solutionResponse.solution[selectedVariable];
  const { setStructure, solutions } = variableData;

  // Check if all objective values are binary (0 or 1)
  const isBinary = solutions.every(
    (sol) => sol.objectiveValue === 0 || sol.objectiveValue === 1
  );

  return (
    <div className="solution-results-page">
      <h1 className="page-title">Solution Results</h1>

      {/* Dropdown to Select Variable */}
      <div className="solution-dropdown-container">
      <div className="solution-dropdown">
      <label>Select Variable: </label>
      
      <select onChange={handleVariableChange} value={selectedVariable}>
        {Object.keys(solutionResponse.solution).map((variable) => (
          <option key={variable} value={variable}>
            {variable}
          </option>
        ))}
      </select>
      </div>
      </div>
      {/* Render Table Based on setStructure.length */}
      <div className="solution-table-container">
      {setStructure.length === 2 && (
        <table className="solution-table">
          <thead>
            <tr>
              <th>
                {setStructure[1]} \ {setStructure[0]}
              </th>
              {[...new Set(solutions.map((sol) => sol.values[1]))].map(
                (col, index) => (
                  <th key={index}>{col}</th>
                )
              )}
            </tr>
          </thead>
          <tbody>
            {[...new Set(solutions.map((sol) => sol.values[0]))].map(
              (row, rowIndex) => (
                <tr key={rowIndex}>
                  <td>{row}</td>
                  {[...new Set(solutions.map((sol) => sol.values[1]))].map(
                    (col, colIndex) => {
                      const match = solutions.find(
                        (sol) => sol.values[0] === row && sol.values[1] === col
                      );
                      return (
                        <td key={colIndex}>
                        {match ? (
                            isBinary ? (
                                <span className={`binary-value ${match.objectiveValue === 1 ? "v" : "x"}`}>
                                    {match.objectiveValue === 1 ? "✔" : "✖"}
                                </span>
                            ) : (
                                match.objectiveValue
                            )
                        ) : (
                            <span className="binary-value x">✖</span> 
                        )}
                    </td>
                    
                      );
                    }
                  )}
                </tr>
              )
            )}
          </tbody>
        </table>
      )}

      {setStructure.length === 3 && (
        <table className="solution-table">
          <thead>
            <tr>
              <th>
                {setStructure[2]} \ {setStructure[1]}
              </th>
              {[...new Set(solutions.map((sol) => sol.values[2]))].map(
                (col, index) => (
                  <th key={index}>{col}</th>
                )
              )}
            </tr>
          </thead>
          <tbody>
            {[...new Set(solutions.map((sol) => sol.values[1]))].map(
              (row, rowIndex) => (
                <tr key={rowIndex}>
                  <td>{row}</td>
                  {[...new Set(solutions.map((sol) => sol.values[2]))].map(
                    (col, colIndex) => {
                      const relevantPeople = solutions.filter(
                        (sol) => sol.values[1] === row && sol.values[2] === col
                      );
                      return (
                        <td key={colIndex}>
                          <table className="mini-table">
                            <thead>
                              <tr>
                                {relevantPeople.map((sol, pIndex) => (
                                  <th key={pIndex}>{sol.values[0]}</th>
                                ))}
                              </tr>
                            </thead>
                            <tbody>
                              <tr>
                                {relevantPeople.map((sol, pIndex) => (
                                  <td key={colIndex}>
                                    {sol ? (
                                      isBinary ? (
                                        <span
                                          className={`binary-value ${
                                            sol.objectiveValue === 1
                                              ? "v"
                                              : "x"
                                          }`}
                                        >
                                          {sol.objectiveValue === 1
                                            ? "✔"
                                            : "✖"}
                                        </span>
                                      ) : (
                                        sol.objectiveValue
                                      )
                                    ) : (
                                      <span className="binary-value x">✖</span>
                                    )}
                                  </td>
                                ))}
                              </tr>
                            </tbody>
                          </table>
                        </td>
                      );
                    }
                  )}
                </tr>
              )
            )}
          </tbody>
        </table>
      )}
    </div>
    </div>
  );
};

export default SolutionResultsPage;
