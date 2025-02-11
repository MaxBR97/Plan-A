import React, { useState } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./SolutionPreviewPage.css";
import { useNavigate } from "react-router-dom"; // Import useNavigate

const SolutionPreviewPage = () => {
  const {
    constraints,
    preferences,
    modules,
    preferenceModules,
    variables,
    types,
    imageId,
    setSolutionResponse,
    setTypes,
    paramTypes,
    setSolutionData,
    variablesModule,
  } = useZPL();

  const allSets = variables.flatMap(
    (variable) => variable.dep?.setDependencies ?? []
  );
  const [variableValues, setVariableValues] = useState({});
  const [paramValues, setParamValues] = useState({});
  const [constraintsToggledOff, setConstraintsToggledOff] = useState([]);

  const navigate = useNavigate(); // Initialize navigation
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

  const getNumTypes = (typeInfo) => {
    if (!typeInfo) {
      console.warn("⚠️ Warning: getNumTypes received undefined typeInfo.");
      return 1; // Default to 1 to prevent errors
    }

    return Array.isArray(typeInfo) ? typeInfo.length : 1;
  };

  const handleAddVariable = (setName) => {
    console.log("Adding Variable for:", setName);
    console.log("Available setTypes:", setTypes);

    if (!setTypes[setName]) {
      console.error(`❌ Error: setTypes does not contain ${setName}`);
      return; // Prevent further execution
    }

    const numTypes = getNumTypes(setTypes[setName]); // Function to extract type count

    setVariableValues((prev) => ({
      ...prev,
      [setName]: [...(prev[setName] || []), new Array(numTypes).fill("")],
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

  const handleToggleConstraint = (moduleName) => {
    setConstraintsToggledOff((prev) =>
      prev.includes(moduleName)
        ? prev.filter((name) => name !== moduleName)
        : [...prev, moduleName]
    );
  };
  const [preferencesToggledOff, setPreferencesToggledOff] = useState([]);

  const handleTogglePreference = (preferenceName) => {
    setPreferencesToggledOff(
      (prev) =>
        prev.includes(preferenceName)
          ? prev.filter((name) => name !== preferenceName) // Remove if exists
          : [...prev, preferenceName] // Add if not exists
    );
  };
/*
  const handleSolve = async () => {
    setErrorMessage(null); // Reset previous error
    setResponseData(null); // Clear local response

    const transformedParamValues = Object.fromEntries(
      Object.entries(paramValues).map(([key, value]) => [
        key,
        [parseFloat(value) || 0],
      ]) // Ensures values are arrays of numbers
    );

    const requestBody = {
      imageId,
      input: {
        setsToValues: variableValues,
        paramsToValues: transformedParamValues,
        constraintsToggledOff: [],
        preferencesToggledOff: [],
      },
      timeout: 30,
    };

    console.log("Sending request:", JSON.stringify(requestBody, null, 2));

    try {
      const response = await fetch("/solve", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      const responseText = await response.text(); // Read response as text (for error handling)

      if (!response.ok) {
        console.error("Server returned an error:", responseText);
        throw new Error(
          `HTTP Error! Status: ${response.status} - ${responseText}`
        );
      }

      const data = JSON.parse(responseText); // Parse response if it's valid JSON
      setSolutionResponse(data); // Store response in context

      navigate("/solution-results"); // Redirect user to the results page
    } catch (error) {
      console.error("Error solving problem:", error);
      setErrorMessage(`Failed to solve. ${error.message}`);
    }
  };
*/

const handleSolve = async () => {
  setErrorMessage(null);
  setResponseData(null);

  // Construct the PATCH request body
  const patchRequestBody = {
      imageId,
      image: { // Wrap everything under "image"
          variablesModule, // Assumed to be available in context
          constraintModules: modules.map(module => ({
              moduleName: module.name,
              constraints: module.constraints.map(c => c.identifier),
              inputSets: module.involvedSets,
              inputParams: module.involvedParams,
              moduleDescription: module.description
          })),
          preferenceModules: preferenceModules.map(module => ({
              moduleName: module.name,
              preferences: module.preferences.map(p => p.identifier),
              inputSets: module.involvedSets,
              inputParams: module.involvedParams,
              moduleDescription: module.description
          }))
      }
  };

  console.log("Sending PATCH request:", JSON.stringify(patchRequestBody, null, 2));

  try {
      // PATCH request to /Images
      const patchResponse = await fetch("/images", {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(patchRequestBody)
      });

      if (!patchResponse.ok) {
          throw new Error(`PATCH request failed! Status: ${patchResponse.status}`);
      }

      console.log("✅ PATCH request successful!");

  } catch (error) {
      console.error("Error sending PATCH request:", error);
      setErrorMessage(`Failed to update image metadata: ${error.message}`);
      return; // Stop execution if PATCH fails
  }

  // Construct the POST request body for solving
  const transformedParamValues = Object.fromEntries(
      Object.entries(paramValues).map(([key, value]) => [
          key,
          [parseFloat(value) || 0]
      ])
  );

  const requestBody = {
      imageId,
      input: {
          setsToValues: variableValues,
          paramsToValues: transformedParamValues,
          constraintsToggledOff: constraintsToggledOff,
          preferencesToggledOff: preferencesToggledOff
      },
      timeout: 30
  };

  console.log("Sending POST request:", JSON.stringify(requestBody, null, 2));

  try {
      const response = await fetch("/solve", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(requestBody)
      });

      const responseText = await response.text();

      if (!response.ok) {
          console.error("Server returned an error:", responseText);
          throw new Error(`HTTP Error! Status: ${response.status} - ${responseText}`);
      }
      
      const data = JSON.parse(responseText);
      console.log(responseText);
      console.log(data);
      setSolutionResponse(data);
      navigate("/solution-results");
  } catch (error) {
      console.error("Error solving problem:", error);
      setErrorMessage(`Failed to solve. ${error.message}`);
  }
};


  const [responseData, setResponseData] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const fakeResponse = {
    solved: true,
    solvingTime: 12.5,
    objectiveValue: 100.23,
    solution: {
      Soldier_shift: {
        setStructure: ["C", "Stations", "Times"],
        typeStructure: ["INT", "TEXT", "INT"],
        solutions: [
          {
            values: ["1", "Fillbox", "8"],
            objectiveValue: 1,
          },
          {
            values: ["2", "Fillbox", "16"],
            objectiveValue: 1,
          },
          {
            values: ["3", "Fillbox", "0"],
            objectiveValue: 1,
          },
          {
            values: ["4", "Fillbox", "20"],
            objectiveValue: 1,
          },
          {
            values: ["2", "Shin Gimel", "16"],
            objectiveValue: 1,
          },
          {
            values: ["3", "Shin Gimel", "16"],
            objectiveValue: 1,
          },
          {
            values: ["4", "Shin Gimel", "16"],
            objectiveValue: 1,
          },
          {
            values: ["5", "Shin Gimel", "16"],
            objectiveValue: 1,
          },
        ],
      },
      minGuards: {
        setStructure: ["X", "Y"],
        typeStructure: ["INT"],
        solutions: [
          {
            values: ["X val", "Y val"],
            objectiveValue: 30,
          },
        ],
      },
    },
  };
  const handleFakeResponse = () => {
    setSolutionResponse(fakeResponse); // ✅ Store the fake response in context
    navigate("/solution-results"); // ✅ Redirect to the next screen
  };

  const selectedParams = variablesModule?.variablesConfigurableParams ?? [];


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
                {/* Toggle Button Positioned Correctly */}
                <div className="toggle-container">
                  <label className="switch">
                    <input
                      type="checkbox"
                      checked={!constraintsToggledOff.includes(module.name)}
                      onChange={() => handleToggleConstraint(module.name)}
                    />
                    <span className="slider round"></span>
                  </label>
                </div>

                <h3 className="module-title">{module.name}</h3>
                <p className="module-description">
                  <strong>Module Description:</strong> {module.description}
                </p>

                <h4 className="module-subtitle">Constraints</h4>
                {module.constraints.length > 0 ? (
                  module.constraints.map((constraint, cIndex) => (
                    <div key={cIndex} className="module-item">
                      <p>{constraint.identifier}</p>{" "}
                      {/* Only displaying the identifier value */}
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
                {/* Toggle Button Positioned Correctly */}
                <div className="toggle-container">
                  <label className="switch">
                    <input
                      type="checkbox"
                      checked={!preferencesToggledOff.includes(module.name)}
                      onChange={() => handleTogglePreference(module.name)}
                    />
                    <span className="slider round"></span>
                  </label>
                </div>

                <h3 className="module-title">{module.name}</h3>
                <p className="module-description">
                  <strong>Module Description:</strong> {module.description}
                </p>

                <h4 className="module-subtitle">Preferences</h4>
                {module.preferences.length > 0 ? (
                  module.preferences.map((preference, pIndex) => (
                    <div key={pIndex} className="module-item">
                      <p>{preference.identifier}</p>{" "}
                      {/* Removed "Identifier:" */}
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
        {/* Variable Sets Section */}
<div className="module-section">
  <h2 className="section-title">Variable Sets</h2>
  {Array.from(new Set(Object.keys(setTypes)))
    .filter((set) => variablesModule?.variablesConfigurableSets.includes(set))
    .map((set, index) => {
      // Fetch type from setTypes
      const typeList = setTypes[set]
        ? Array.isArray(setTypes[set])
          ? setTypes[set]
          : [setTypes[set]]
        : ["Unknown"];

      return (
        <div key={index} className="module-box">
          {/* Display Variable Name */}
          <h3 className="module-title">{set}</h3>

          {/* Display Type from setTypes */}
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
                    placeholder={`Enter ${typeList[typeIndex] || "value"}:`}
                  />
                );
              })}

              {/* Add a divider after each row */}
              <hr className="input-divider" />
            </div>
          ))}
        </div>
      );
    })}
</div>

        {/* Variable Parameters Section */}
         {/* Parameters Section */}
      <div className="module-section">
        <h2 className="section-title">Parameters</h2>
        {Object.keys(paramTypes)
          .filter((param) => selectedParams.includes(param))
          .map((param, index) => (
            <div key={index} className="module-box">
              <h3 className="module-title">{param}</h3>
              <p className="variable-type">
                <strong>Type:</strong> {paramTypes[param] || "Unknown"}
              </p>
              {/* Input field for each parameter */}
              <input
                type="text"
                value={paramValues[param] || ""}
                onChange={(e) => handleParamChange(param, e.target.value)}
                className="variable-input"
                placeholder={`Enter ${paramTypes[param] || "value"}...`}
              />
            </div>
          ))}
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

        {/* Fake Response Button */}
        <button className="fake-response-button" onClick={handleFakeResponse}>
          Fake Solve Response
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
