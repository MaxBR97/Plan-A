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
import LogBoard from "../reusableComponents/LogBoard.js"
import { useEffect } from "react";
import { useNavigate } from "react-router-dom"; // Import useNavigate

import WebSocketTester from "./WebSocketTest.js";


const SolutionPreviewPage = ({isDesktop=false}) => {
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
  const [errorMessage, setErrorMessage] = useState(null);
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
          
          { preferenceModules.length > 0 && (
            Array.from(costParams).length > 0 ? <DraggableBar 
            min={0}
            max={100}
            markers={Array.from(costParams.keys())
              .filter(param => paramValues[param])
              .map(param => ({ [param]: parseFloat(paramValues[param][0]) }))
            }
            costParams={costParams} // <--- pass the costParams!
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
  {variablesModule.inputSets.filter(set => !variablesModule.variablesOfInterest.some(v => v.boundSet === set.name))
                            .map((setDef, index) => (
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

        <div className="results">
          <SolutionResultsPage 
            // globalSelectedTuples={globalSelectedTuples} 
            // setGlobalSelectedTuples={setGlobalSelectedTuples}
            image={image}
            variableValues={variableValues}
            selectedVariableValues={selectedVariableValues}
            paramValues={paramValues}
            constraintsToggledOff={constraintsToggledOff}
            preferencesToggledOff={preferencesToggledOff}
            isDesktop={isDesktop}
          />
        </div>

        <button className="home-button" onClick={() => navigate("/")}>
          ← Back to Home
        </button>
      </div>
      <Link to="/configuration-menu" className="back-button">
        Back to Configuration
      </Link>

      {/* <WebSocketTester /> */}
      
    </div>
    
  );
};

export default SolutionPreviewPage;
