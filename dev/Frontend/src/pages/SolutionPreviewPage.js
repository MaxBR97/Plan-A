import React, { useState } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./SolutionPreviewPage.css";
import SolutionResultsPage from "./SolutionResultsPage.js";
import NumberInput from '../reusableComponents/NumberInput';
import SetEntry from '../reusableComponents/SetEntry';
import { useEffect } from "react";
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
  const [selectedVariableValues, setSelectedVariableValues] = useState({});
  const [constraintsToggledOff, setConstraintsToggledOff] = useState([]);
  const [showResults, setShowResults] = useState(true);
  const [timeout, setTimeout] = useState(10);
  const navigate = useNavigate(); // Initialize navigation
  const handleAddValue = (setName) => {
    setVariableValues((prev) => ({
      ...prev,
      [setName]: [...(prev[setName] || []), ""],
    }));
  };

  const isRowSelected = (setName, rowIndex) => {
    return selectedVariableValues[setName]?.includes(rowIndex) || false;
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
  
    setVariableValues((prev) => {
      const newRow = new Array(numTypes).fill("");
      const updatedValues = [...(prev[setName] || []), newRow];
      
      // Calculate the rowIndex of the newly added row
      const rowIndex = updatedValues.length - 1;
  
      setSelectedVariableValues((selectedPrev) => ({
        ...selectedPrev,
        [setName]: [...(selectedPrev[setName] || []), rowIndex],
      }));
  
      return {
        ...prev,
        [setName]: updatedValues,
      };
    });
  };
  
  const handleRemoveVariable = (setName, rowIndex) => {
    setVariableValues((prev) => {
      const updatedValues = [...(prev[setName] || [])];
      updatedValues.splice(rowIndex, 1); // Remove the row at rowIndex
      return { ...prev, [setName]: updatedValues };
    });
  
    setSelectedVariableValues((prev) => {
      const updated = { ...prev };
      const isSelected = updated[setName].includes(rowIndex);
      if (isSelected) {
        updated[setName] = updated[setName].filter((index) => index !== rowIndex);
      }
      // Adjust selected indexes after removal
      if(updated[setName]){
          updated[setName] = updated[setName].map(index => index > rowIndex ? index -1 : index);
      }
  
      return updated;
    });
  };
  
  const handleVariableChange = (setName, rowIndex, typeIndex, value) => {
    setVariableValues((prev) => {
      const updatedValues = [...(prev[setName] || [])];
      updatedValues[rowIndex] = [...updatedValues[rowIndex]]; // Copy row to avoid mutation
      updatedValues[rowIndex][typeIndex] = value; // Update only the correct type input
      return { ...prev, [setName]: updatedValues };
    });
  };
  
  const handleVariableToggle = (setName, rowIndex) => {
    setSelectedVariableValues((prev) => {
      const updated = { ...prev };
  
      if (!updated[setName]) {
        updated[setName] = [];
      }
  
      const isSelected = updated[setName].includes(rowIndex);
      if (isSelected) {
        updated[setName] = updated[setName].filter((index) => index !== rowIndex);
      } else {
        updated[setName] = [...updated[setName], rowIndex];
      }
  
      return updated;
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

const loadInputs = async () => {
  try {
      const response = await fetch(`/images/${imageId}/inputs`, {
          method: "GET",
          headers: { "Content-Type": "application/json" },
      });

      if (!response.ok) {
          throw new Error(`load inputs request failed! Status: ${response.status}`);
      }

      const responseText = await response.text(); // Wait for response body
      const data = JSON.parse(responseText);

     
        setVariableValues(data.setsToValues);
        setParamValues(data.paramsToValues);
      
      
  } catch (error) {
      console.error("Error fetching inputs:", error);
      setErrorMessage(`Failed to fetch inputs: ${error.message}`);
  }
};

const patchConfigurations = async () => {
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
}

useEffect(() => {
  (async () => {
    await patchConfigurations();
    await loadInputs();
    console.log("Inputs loaded, now user can proceed.");
  })();
}, []);
const handleSolve = async () => {
  setErrorMessage(null);
  setResponseData(null);

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
      setsToValues: Object.entries(variableValues).reduce((acc, [setName, rows]) => {
        if (selectedVariableValues[setName]) {
          const selectedRows = rows.filter((_, rowIndex) =>
            selectedVariableValues[setName].includes(rowIndex)
          );
          if (selectedRows.length > 0) {
            acc[setName] = selectedRows;
          }
        }
        return acc;
      }, {}),
      paramsToValues: transformedParamValues,
      constraintsToggledOff: constraintsToggledOff,
      preferencesToggledOff: preferencesToggledOff,
    },
    timeout: timeout,
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
      //navigate("/solution-results");\
      setShowResults(true)
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
    //navigate("/solution-results"); // ✅ Redirect to the next screen
    setShowResults(true);
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
                  <SetEntry
                    key={typeIndex}
                    type="text"
                    value={value}
                    checked={isRowSelected(set, rowIndex)}
                    onEdit={(e) =>
                      handleVariableChange(
                        set,
                        rowIndex,
                        typeIndex,
                        e.target.value
                      )
                    }
                    onToggle={(e) =>
                      handleVariableToggle(
                        set,
                        rowIndex,
                      )
                    }
                    onDelete={(e) =>
                      handleRemoveVariable(
                        set,
                        rowIndex,
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

        {/* Timeout Input */}
        <div className="p-4">
          <NumberInput 
            value={timeout}   
            onChange={setTimeout}
            label="Timeout: "
            placeholder="Enter amount"
            min="0"
          />
        </div>

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
        <div className="results">
        {showResults && <SolutionResultsPage />}
        </div>
      </div>

      <Link to="/configure-constraints" className="back-button">
        Back
      </Link>
    </div>
  );
};

export default SolutionPreviewPage;
