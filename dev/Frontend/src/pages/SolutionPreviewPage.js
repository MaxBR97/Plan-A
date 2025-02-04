import React, { useState } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./SolutionPreviewPage.css";

const SolutionPreviewPage = () => {
  const {
    constraints,
    preferences,
    modules,
    preferenceModules,
    variables,
    types,
    imageId,
  } = useZPL();

  const allSets = variables.flatMap(
    (variable) => variable.dep?.setDependencies ?? []
  );
  const [variableValues, setVariableValues] = useState({});
  const [paramValues, setParamValues] = useState({});

  const handleAddValue = (setName) => {
    setVariableValues((prev) => ({
      ...prev,
      [setName]: [...(prev[setName] || []), ""],
    }));
  };

  const handleValueChange = (setName, index, value) => {
    setVariableValues((prev) => {
      const newValues = [...prev[setName]];
      newValues[index] = value;
      return { ...prev, [setName]: newValues };
    });
  };

  const handleParamChange = (paramName, value) => {
    setParamValues((prev) => ({
      ...prev,
      [paramName]: value,
    }));
  };

  const getNumTypes = (setName) => {
    const typeValue = types[setName]; // Get the type(s) for the given set

    if (!typeValue) return 1; // Default to 1 if type is missing

    if (typeof typeValue === "string") {
      const typeList = typeValue.replace(/[<>]/g, "").split(","); // Remove <> and split by comma
      return typeList.length; // Return the number of types
    }

    return Array.isArray(typeValue) ? typeValue.length : 1; // Handle already-parsed arrays
  };

  const handleAddVariable = (setName) => {
    const numTypes = getNumTypes(setName); // Get the correct number of types

    setVariableValues((prev) => ({
      ...prev,
      [setName]: [...(prev[setName] || []), Array(numTypes).fill("")], // Add N empty inputs per row
    }));
  };

  const handleVariableChange = (setName, rowIndex, typeIndex, value) => {
    setVariableValues((prev) => {
      const updatedValues = [...(prev[setName] || [])];
      updatedValues[rowIndex] = [...updatedValues[rowIndex]]; // Copy row to avoid mutation
      updatedValues[rowIndex][typeIndex] = value; // Update only the correct type input
      return { ...prev, [setName]: updatedValues };
    });
  };

  const handleSolve = async () => {
    setErrorMessage(null); // Reset any previous error messages
    setResponseData(null); // Clear previous response data

    const requestBody = {
      imageId,
      input: {
        setsToValues: variableValues, // User inputted set values
        paramsToValues: paramValues, // User inputted param values
        constraintsToggledOff: [], // For now, empty array
        preferencesToggledOff: [], // For now, empty array
      },
    };

    console.log("Sending request:", JSON.stringify(requestBody, null, 2));

    try {
      const response = await fetch("/solve", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        throw new Error(`HTTP Error! Status: ${response.status}`);
      }

      const data = await response.json();
      setResponseData(data);
      setShowModal(true);
    } catch (error) {
      console.error("Error solving problem:", error);
      setErrorMessage(`Failed to solve. ${error.message}`);
    }
  };

  const [responseData, setResponseData] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  return (
    <div className="solution-preview-page">
      <h1 className="page-title">Solution Preview</h1>
      <div className="modules-container">
        {/* Constraints Section */}
        <div className="module-section">
          <h2 className="section-title">Constraints</h2>
          {modules.length > 0 ? (
            modules.map((module, index) => (
              <div key={index} className="module-box">
                <h3 className="module-title">{module.name}</h3>
                <p className="module-description">
                  <strong>Module Description:</strong> {module.description}
                </p>
                <h4 className="module-subtitle">Constraints</h4>
                {module.constraints.length > 0 ? (
                  module.constraints.map((constraint, cIndex) => (
                    <div key={cIndex} className="module-item">
                      <p>
                        <strong>Identifier:</strong> {constraint.identifier}
                      </p>
                    </div>
                  ))
                ) : (
                  <p className="empty-message">
                    No constraints in this module.
                  </p>
                )}
                <h4 className="module-subtitle">Involved Sets</h4>
                {module.involvedSets.length > 0 ? (
                  module.involvedSets.map((set, sIndex) => (
                    <div key={sIndex} className="module-item">
                      {set}
                    </div>
                  ))
                ) : (
                  <p className="empty-message">No involved sets.</p>
                )}
                <h4 className="module-subtitle">Involved Parameters</h4>
                {module.involvedParams.length > 0 ? (
                  module.involvedParams.map((param, pIndex) => (
                    <div key={pIndex} className="module-item">
                      {param}
                    </div>
                  ))
                ) : (
                  <p className="empty-message">No involved parameters.</p>
                )}
              </div>
            ))
          ) : (
            <p className="empty-message">No constraint modules available.</p>
          )}
        </div>

        {/* Preferences Section */}
        <div className="module-section">
          <h2 className="section-title">Preferences</h2>
          {preferenceModules.length > 0 ? (
            preferenceModules.map((module, index) => (
              <div key={index} className="module-box">
                <h3 className="module-title">{module.name}</h3>
                <p className="module-description">
                  <strong>Module Description:</strong> {module.description}
                </p>
                <h4 className="module-subtitle">Preferences</h4>
                {module.preferences.length > 0 ? (
                  module.preferences.map((preference, pIndex) => (
                    <div key={pIndex} className="module-item">
                      <p>
                        <strong>Identifier:</strong> {preference.identifier}
                      </p>
                    </div>
                  ))
                ) : (
                  <p className="empty-message">
                    No preferences in this module.
                  </p>
                )}
                <h4 className="module-subtitle">Involved Sets</h4>
                {module.involvedSets.length > 0 ? (
                  module.involvedSets.map((set, sIndex) => (
                    <div key={sIndex} className="module-item">
                      {set}
                    </div>
                  ))
                ) : (
                  <p className="empty-message">No involved sets.</p>
                )}
                <h4 className="module-subtitle">Involved Parameters</h4>
                {module.involvedParams.length > 0 ? (
                  module.involvedParams.map((param, pIndex) => (
                    <div key={pIndex} className="module-item">
                      {param}
                    </div>
                  ))
                ) : (
                  <p className="empty-message">No involved parameters.</p>
                )}
              </div>
            ))
          ) : (
            <p className="empty-message">No preference modules available.</p>
          )}
        </div>

        {/* Variable Sets Section */}
        <div className="module-section">
          <h2 className="section-title">Variable Sets</h2>
          {Array.from(new Set(allSets)).map((set, index) => {
            // Remove duplicates
            const typeList = Array.isArray(types[set])
              ? types[set]
              : [types[set]]; // Ensure it's an array

            return (
              <div key={index} className="module-box">
                {/* Display Variable Name */}
                <h3 className="module-title">{set}</h3>

                {/* Display Type */}
                <p className="variable-type">
                  <strong>Type:</strong> {typeList.join(", ")}
                </p>

                {/* Add Button */}
                <button
                  className="add-button"
                  onClick={() => handleAddVariable(set, typeList)}
                ></button>

                {/* Input Fields - Each type gets its own separate textbox */}
                {variableValues[set]?.map((row, rowIndex) => (
                  <div key={rowIndex} className="input-row">
                    {row.map((value, typeIndex) => {
                      // Extract and format types correctly
                      const typeList = types[set]
                        ? types[set].replace(/[<>]/g, "").split(",")
                        : ["value"];
                      return (
                        <input
                          key={typeIndex}
                          type="text"
                          value={value}
                          onChange={(e) =>
                            handleVariableChange(
                              set,
                              rowIndex,
                              typeIndex,
                              e.target.value
                            )
                          }
                          className="variable-input"
                          placeholder={`Enter ${
                            typeList[typeIndex]?.trim() || "value"
                          }:`}
                        />
                      );
                    })}

                    {/* ✅ Add a divider after each row */}
                    <hr className="input-divider" />
                  </div>
                ))}
              </div>
            );
          })}
        </div>

        {/* Variable Parameters Section */}
<div className="module-section">
    <h2 className="section-title">Variable Parameters</h2>
    {Object.keys(types).map((param, index) => {
        // Ensure the parameter is not a variable set (i.e., it's a standalone parameter)
        if (!allSets.includes(param)) {
            return (
                <div key={index} className="module-box">
                    {/* Display Parameter Name */}
                    <h3 className="module-title">{param}</h3>

                    {/* Display Parameter Type */}
                    <p className="variable-type">
                        <strong>Type:</strong> {types[param] || "Unknown"}
                    </p>

                    {/* Input Field */}
                    <input 
                        type="text" 
                        value={paramValues[param] || ''} 
                        onChange={(e) => handleParamChange(param, e.target.value)} 
                        className="variable-input" 
                        placeholder={`Enter ${types[param] || "value"}...`}
                    />
                </div>
            );
        }
        return null; // Skip variables, only show params
    })}
</div>


        {/* Error Message */}
        {errorMessage && (
          <div className="error-container">
            <p className="error-message">{errorMessage}</p>
          </div>
        )}

        {/* Solve Button */}

        <button className="solve-button" onClick={handleSolve}>
          Solve
        </button>
        {/* Modal for Response */}
        {showModal && (
          <div className="response-modal">
            <div className="modal-content">
              <span
                className="close-button"
                onClick={() => setShowModal(false)}
              >
                ×
              </span>
              <h2>Solution Response</h2>
              <pre>{JSON.stringify(responseData, null, 2)}</pre>
            </div>
          </div>
        )}
      </div>

      <Link to="/configure-constraints" className="back-button">
        Back
      </Link>
    </div>
  );
};

export default SolutionPreviewPage;
