import React, { useState } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./SolutionPreviewPage.css";

const SolutionPreviewPage = () => {
  const {imageId, constraints, preferences, modules, preferenceModules, variables } =
    useZPL();
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

        {/* Variable Parameters Section */}
        <div className="module-section">
          <h2 className="section-title">Variable Parameters</h2>
          {variables.length > 0 ? (
            variables
              .flatMap((variable) => variable.dep?.paramDependencies || [])
              .map((param, index) => (
                <div key={index} className="module-box">
                  <h3 className="module-title">{param}</h3>
                  <input
                    type="text"
                    value={paramValues[param] || ""}
                    onChange={(e) => handleParamChange(param, e.target.value)}
                    className="variable-input"
                    placeholder="Enter value..."
                  />
                </div>
              ))
          ) : (
            <p className="empty-message">No parameters available.</p>
          )}
        </div>

        {/* Variable Sets Section */}
        <div className="module-section">
          <h2 className="section-title">Variable Sets</h2>
          {variables.map((variable, index) =>
            variable.dep?.setDependencies?.map((set, sIndex) => (
              <div key={`${index}-${sIndex}`} className="module-box">
                <h3 className="module-title">{set}</h3>
                <button
                  className="add-button"
                  onClick={() => handleAddValue(set)}
                ></button>
                {variableValues[set]?.map((value, vIndex) => (
                  <input
                    key={vIndex}
                    type="text"
                    value={value}
                    onChange={(e) =>
                      handleValueChange(set, vIndex, e.target.value)
                    }
                    className="variable-input"
                  />
                ))}
              </div>
            ))
          )}
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
