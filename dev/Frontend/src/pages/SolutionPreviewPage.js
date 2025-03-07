import React, { useState } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./SolutionPreviewPage.css";
import SolutionResultsPage from "./SolutionResultsPage.js";
import NumberInput from '../reusableComponents/NumberInput';
import SetEntry from '../reusableComponents/SetEntry';
import ModuleBox from '../reusableComponents/ModuleBox.js';
import SetInputBox from '../reusableComponents/SetInputBox.js';
import ParameterInputBox from "../reusableComponents/ParameterInputBox";
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
    setTags,
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
  const [preferencesToggledOff, setPreferencesToggledOff] = useState([]);
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
    // Ensure value is always an array of strings
    const formattedValue = Array.isArray(value) ? value : [String(value)];
    
    setParamValues((prev) => ({
      ...prev,
      [paramName]: formattedValue,
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

  useEffect(() => {
  }, [constraintsToggledOff, preferencesToggledOff]);


  const handleToggleConstraint = (moduleName) => {
    setConstraintsToggledOff((prev) =>
      prev.includes(moduleName)
        ? prev.filter((name) => name !== moduleName)
        : [...prev, moduleName]
    );
  };

  

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
        
      const preSelectedVariables = {};
      Object.keys(data.setsToValues).forEach((setName) => {
        preSelectedVariables[setName] = data.setsToValues[setName].map((_, index) => index);
      });

    setSelectedVariableValues(preSelectedVariables);
      
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
          value
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
      constraintModulesToggledOff: constraintsToggledOff,
      preferenceModulesToggledOff: preferencesToggledOff,
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

  const selectedParams = variablesModule?.variablesConfigurableParams ?? [];

  return (
    <div className="solution-preview-page">
      <h1 className="page-title">Solution Preview</h1>
      <div className="modules-container">
        {/* Constraints Section */}
        <div className="module-section">
          <h2 className="section-title">Constraints</h2>
          {modules.length > 0 ? (
            modules.map((module, index) => {
              let inputSets = {};
              let inputParams = {}

              module.inputSets.forEach((setName) => {
                const inputSet = {
                  setName: setName,
                  typeList: setTypes[setName],
                  tupleTags: [],
                  setValues: variableValues[setName],
                };

                inputSets[setName] = inputSet; 
              });

              module.inputParams.forEach((paramName) => {
                const inputParam = {
                  paramName: paramName,
                  value: paramValues[paramName],
                  type: paramTypes[paramName]
                };
                inputParams[paramName] = inputParam;
              });
              
              return (
              <ModuleBox
              key={index}
              module={module}
              checked={!constraintsToggledOff.includes(module.name)}
              handleToggleModule={handleToggleConstraint}
              handleAddTuple={handleAddVariable}
              handleRemoveTuple={handleRemoveVariable}
              handleTupleToggle={handleVariableToggle}
              handleTupleChange={handleVariableChange}
              handleParamChange={handleParamChange}
              isRowSelected={isRowSelected}
              inputSets={inputSets}
              inputParams={inputParams}
              />
            )})
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
        <SetInputBox
          index={index}
          typeList={typeList}
          tupleTags={setTags || []}
          setName={set}
          handleAddTuple={handleAddVariable}
          handleTupleChange={handleVariableChange}
          handleTupleToggle={handleVariableToggle}
          handleRemoveTuple={handleRemoveVariable}
          isRowSelected={isRowSelected}
          setValues={variableValues[set]}
        />
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
          <ParameterInputBox
            key={index}
            paramName={param}
            type={paramTypes[param]}
            value={paramValues[param]}
            onChange={handleParamChange}
          />
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
