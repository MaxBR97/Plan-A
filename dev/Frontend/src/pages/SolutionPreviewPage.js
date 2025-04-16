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
import DraggableBar from "../reusableComponents/DraggableBar.js";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom"; // Import useNavigate


const SolutionPreviewPage = () => {
  const {
    image,
    model,
    solutionResponse,
    updateImage,
    updateImageField,
    updateModel,
    updateSolutionResponse,
    initialImageState
  } = useZPL();

  const [variableValues, setVariableValues] = useState({});
  const [paramValues, setParamValues] = useState({});
  const [selectedVariableValues, setSelectedVariableValues] = useState({});
  const [constraintsToggledOff, setConstraintsToggledOff] = useState([]);
  const [preferencesToggledOff, setPreferencesToggledOff] = useState([]);
  const [sets, setSets] = useState(new Map());
  const [params, setParams] = useState(new Map());
  const [costParams, setCostParams] = useState(new Map());
  const [constraintModules, setConstraintModules] = useState(Array.from(image.constraintModules));
  const [preferenceModules, setPreferenceModules] = useState(Array.from(image.preferenceModules));
  const [variablesModule, setVariablesModule] = useState(image.variablesModule);
  const [variables, setVariables] = useState([]);
  const [showResults, setShowResults] = useState(false);
  const [timeout, setTimeout] = useState(10);
  const [solutionStatus, setSolutionStatus] = useState(null);
  const [globalSelectedTuples, setGlobalSelectedTuples] = useState({});
  const navigate = useNavigate(); 
  

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
    if (!sets.get(setName)) {
      console.error(`❌ Error: setTypes does not contain ${setName}`);
      return; // Prevent further execution
    }
  
    const numTypes = getNumTypes(sets.get(setName).type); // Function to extract type count
  
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

  const handleTogglePreference = (preferenceName) => {
    setPreferencesToggledOff(
      (prev) =>
        prev.includes(preferenceName)
          ? prev.filter((name) => name !== preferenceName) // Remove if exists
          : [...prev, preferenceName] // Add if not exists
    );
  };

  
  const loadImage = async () => {
    try {
      const response = await fetch(`/images/${image.imageId}`, {
        method: "GET",
      });
  
      if (!response.ok) {
        const errorText = await response.text(); // Get error message
        throw new Error(`Load image request failed! Status: ${response.status}, Response: ${errorText}`);
      }
  
    const data = await response.json();
    updateImage(data);
    console.log("Fetched image: ", data)
  
    } catch (error) {
      console.error("Error fetching image:", error);
      setErrorMessage(`Failed to fetch image: ${error.message}`);
    }
  };  

const loadInputs = async () => {
  try {
      console.log("fetching inputs from: ", image.imageId)
      const response = await fetch(`/images/${image.imageId}/inputs`, {
          method: "GET",
          headers: { "Content-Type": "application/json" },
      });

      const responseText = await response.text(); // Wait for response body
      const data = JSON.parse(responseText);

      if (!response.ok) {
        throw new Error(`load inputs request failed! Status: ${responseText}`);
    }
      
      console.log("load input response: ", data)
      const filteredParamsToValues = Object.keys(data.paramsToValues)
          .filter((paramKey) => params.has(paramKey))
          .reduce((filteredObject, paramKey) => {
            filteredObject[paramKey] = data.paramsToValues[paramKey];
            return filteredObject;
          }, {});
      const filteredSetsToValues = Object.keys(data.setsToValues)
          .filter((setKey) => sets.has(setKey))
          .reduce((filteredObject, setKey) => {
            filteredObject[setKey] = data.setsToValues[setKey];
            return filteredObject;
          }, {});

      setVariableValues(filteredSetsToValues);
      setParamValues(filteredParamsToValues);
      

        
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

useEffect(() => {
  (async () => {
    await loadImage();

  })();
}, []);

useEffect(() => {
  // Check if image data is available
  if (image) {
    const newSets = new Map();
    const newParams = new Map();
    const newCostParams = new Map();
    const newConstraintModules = Array.from(image.constraintModules);
    const newPreferenceModules = Array.from(image.preferenceModules);
    const newVariablesModule = image.variablesModule;
    const newVariables = image.variablesModule?.variablesOfInterest ? Array.from(image.variablesModule.variablesOfInterest) : [];

    const processModuleSetsAndParams = (module) => {
      if (module && module.inputSets) {
        module.inputSets.forEach(set => newSets.set(set.name, set));
      }
      if (module && module.inputParams) {
        module.inputParams.forEach(param => newParams.set(param.name, param));
      }
      if (module && module.costParams) {
        module.costParams.forEach(param => newCostParams.set(param.name, param));
      }
    };

    newConstraintModules.forEach(processModuleSetsAndParams);
    newPreferenceModules.forEach(processModuleSetsAndParams);
    if(newVariablesModule){
        processModuleSetsAndParams(newVariablesModule);
    }
 
    setSets(newSets);
    setParams(newParams);
    setCostParams(newCostParams);
    setConstraintModules(newConstraintModules);
    setPreferenceModules(newPreferenceModules);
    setVariablesModule(newVariablesModule);
    setVariables(newVariables);
  } 

  (async () => {
    await loadInputs();
  })();
}, [image]);

const handleSolve = async () => {
  setErrorMessage(null);
  setResponseData(null);
  console.log("global selected:",globalSelectedTuples)
 // Construct the POST request body for solving
const transformedParamValues = Object.fromEntries(
  Object.entries(paramValues).map(([key, value]) => [
      key,
      value
  ])
);

// Create a copy of the existing setsToValues
const updatedSetsToValues = { ...Object.entries(variableValues).reduce((acc, [setName, rows]) => {
if (selectedVariableValues[setName]) {
    const selectedRows = rows.filter((_, rowIndex) =>
        selectedVariableValues[setName].includes(rowIndex)
    );
    if (selectedRows.length > 0) {
        acc[setName] = selectedRows;
    }
}
return acc;
}, {}) };

// First, identify all bound sets from the ImageDTO's variablesOfInterest
const allBoundSets = new Set();
if (image.variablesModule?.variablesOfInterest) {
image.variablesModule.variablesOfInterest.forEach(variable => {
    if (variable.boundSet) {
        allBoundSets.add(variable.boundSet);
    }
});
}

// Initialize all bound sets with empty arrays if they don't exist in updatedSetsToValues
allBoundSets.forEach(boundSetName => {
if (!updatedSetsToValues[boundSetName]) {
    updatedSetsToValues[boundSetName] = [];
}
});

// Process globalSelectedTuples to add bound set data
if (globalSelectedTuples && Object.keys(globalSelectedTuples).length > 0) {
// Iterate through each variable in globalSelectedTuples
Object.entries(globalSelectedTuples).forEach(([variableName, selectedTuples]) => {
    // Find this variable in the image's variablesModule
    const variableInfo = image.variablesModule?.variablesOfInterest?.find(
        v => v.identifier === variableName
    );
    
    // Check if the variable has a bound set
    if (variableInfo && variableInfo.boundSet) {
        const boundSetName = variableInfo.boundSet;
        
        // Check if we have the tuples data
        if (Array.isArray(selectedTuples) && selectedTuples.length > 0) {
            // Transform each tuple to have only values array with objectiveValue appended if present
            const transformedTuples = selectedTuples.map(tuple => {
                if (tuple.hasOwnProperty('objectiveValue') && tuple.objectiveValue !== undefined) {
                    // Create a new tuple with just the values array, appending objectiveValue
                    return [...tuple.values, tuple.objectiveValue];
                    
                }
                // If no objectiveValue or it's undefined, keep just the values array
                return [...tuple.values];
            });
            
            // Add the transformed tuples to the bound set
            transformedTuples.forEach(tuple => {
                if (!updatedSetsToValues[boundSetName].some(
                    existingTuple => JSON.stringify(existingTuple) === JSON.stringify(tuple)
                )) {
                    updatedSetsToValues[boundSetName].push(tuple);
                }
            });
        }
    }
});
}

const requestBody = {
imageId: image.imageId,
input: {
    setsToValues: updatedSetsToValues,
    paramsToValues: transformedParamValues,
    constraintModulesToggledOff: constraintsToggledOff,
    preferenceModulesToggledOff: preferencesToggledOff,
},
timeout: timeout,
};

console.log("global:", globalSelectedTuples);
console.log("request:", requestBody);

try {
let startTime = Date.now(); // Capture the start time

// Start a timer to update solutionStatus every second
const timer = setInterval(() => {
    setSolutionStatus("Solving " + ((Date.now() - startTime) / 1000).toFixed(1)); 
}, 10);
setShowModal(true);
const response = await fetch("/solve", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(requestBody),
});

clearInterval(timer); // Stop the timer once response is received

      const responseText = await response.text();

      if (!response.ok) {
          console.error("Server returned an error:", responseText);
          throw new Error(`HTTP Error! Status: ${response.status} - ${responseText}`);
      }

      const data = JSON.parse(responseText);
      console.log("Solve response: ", data);

      updateSolutionResponse(data);
      setSolutionStatus(data.solutionStatus);
      setShowResults(true);
  } catch (error) {
      console.error("Error solving problem:", error);
      setErrorMessage(`Failed to solve. ${error.message}`);
  }

};

  const [responseData, setResponseData] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  // const selectedParams = image.variablesModule?.inputParams ?? [];
// console.log("Sending SOLVE request:", JSON.stringify(requestBody, null, 2));
  return (
    <div className="solution-preview-page">
      <h1 className="page-title">{image.imageName}</h1>
      <p className="image-description">{image.imageDescription}</p>
      <div className="modules-container">
        {/* Constraints Section */}
        <div className="module-section">
          <h2 className="section-title">Constraints</h2>
          {constraintModules.length > 0 ? (
            constraintModules.map((module, index) => {
              let inputSets = [];
              let inputParams = []

              module.inputSets.forEach((setDef) => {
                const inputSet = {
                  setName: setDef.name,
                  type: setDef.type,
                  tags: setDef.type,
                  alias : setDef.alias,
                  setValues: variableValues[setDef.name] || [] ,
                };

                inputSets =  [...inputSets, inputSet];
              });
              
              module.inputParams.forEach((paramDef) => {
                
                const inputParam = {
                  paramName: paramDef.name,
                  value: paramValues[paramDef.name] || "",
                  type: paramDef.type,
                  alias : paramDef.alias,
                };
                inputParams =[...inputParams, inputParam];
              });
              
              return (
              <ModuleBox
              key={index}
              module={module}
              prefcons={module.constraints}
              checked={!constraintsToggledOff.includes(module.moduleName)}
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
            <p className="empty-message">No constraints modules available.</p>
          )}
        </div>

        {/* Preferences Section */}
        <div className="module-section">
          <h2 className="section-title">Preferences</h2>
          {preferenceModules.length > 0 ? (
            preferenceModules.map((module, index) => {
              let inputSets = [];
              let inputParams = []

              module.inputSets.forEach((setDef) => {
                const inputSet = {
                  setName: setDef.name,
                  type: setDef.type,
                  tags: setDef.type,
                  setValues: variableValues[setDef.name] || [] ,
                };

                inputSets =  [...inputSets, inputSet];
              });
              
              module.inputParams.forEach((paramDef) => {
                
                const inputParam = {
                  paramName: paramDef.name,
                  value: paramValues[paramDef.name] || "",
                  type: paramDef.type
                };
                inputParams =[...inputParams, inputParam];
              });
              
              return (
              <ModuleBox
              key={index}
              module={module}
              prefcons={module.preferences}
              checked={!preferencesToggledOff.includes(module.moduleName)}
              handleToggleModule={handleTogglePreference}
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
            <p className="empty-message">No preference modules available.</p>
          )}
          
          {preferenceModules.length > 0 && (
            Array.from(costParams).length > 0 ?<DraggableBar 
            min={0} 
            max={100} 
            markers = {Array.from(costParams.keys())
            .filter(param => paramValues[param])
            .map(param => ({ [param]: parseFloat(paramValues[param][0]) }))}
            //markers={[{ speed: 30 }, { power: 100 }]} 
            onChange={(marker) => {
              const [paramName, paramValue] = Object.entries(marker)[0];
              handleParamChange(paramName, paramValue);
            }}
            
          />
          
            : null
          )}
        </div>

        {/* Variable Sets Section */}
        
<div className="module-section">
  <h2 className="section-title">Variable Sets</h2>
  {variablesModule.inputSets.map((setDef, index) => (
  <SetInputBox
    index={index}
    typeList={setDef.type}
    tupleTags={setDef.tags}
    setName={setDef.name}
    setAlias={setDef.alias}
    handleAddTuple={handleAddVariable}
    handleTupleChange={handleVariableChange}
    handleTupleToggle={handleVariableToggle}
    handleRemoveTuple={handleRemoveVariable}
    isRowSelected={isRowSelected}
    setValues={variableValues[setDef.name]}
    key={index} //added key prop.
  />
))}
    </div>

            {/* Variable Parameters Section */}
            {/* Parameters Section */}
      <div className="module-section">
      <h2 className="section-title">Parameters</h2>
      {variablesModule.inputParams.map((paramDef, index) => (
          <ParameterInputBox
            key={index}
            paramName={paramDef.name}
            paramAlias={paramDef.alias}
            type={paramDef.type}
            tag={paramDef.tag}
            value={paramValues[paramDef.name]}
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
              <h2>Solution Status:</h2>
              <pre>{solutionStatus}</pre>
            </div>
          </div>
        )}
        <div className="results">
        {showResults && <SolutionResultsPage 
            globalSelectedTuples={globalSelectedTuples} 
            setGlobalSelectedTuples={setGlobalSelectedTuples}
          />}
        </div>
      <button className="home-button" onClick={() => navigate("/")}>
        ← Back to Home
      </button>
      </div>
      <Link to="/configure-constraints" className="back-button">
        Back
      </Link>
    </div>
  );
};

export default SolutionPreviewPage;
