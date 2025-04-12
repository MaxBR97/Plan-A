import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureVariablesPage.css";
import Checkbox from "../reusableComponents/Checkbox.js";
import TagConfigure from "../reusableComponents/TagConfigure.js";

const ConfigureVariablesPage = () => {
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

    const [selectedVars, setSelectedVars] = useState(Array.from(model.variables || []));  // Stores selected variables
    const [displaySets, setDisplaySets] = useState([]);    // Stores sets that should be displayed
    const [displayParams, setDisplayParams] = useState([]); // Stores params that should be displayed
    const [selectedSets, setSelectedSets] = useState([]);  // Stores selected sets
    const [selectedParams, setSelectedParams] = useState([]); // Stores selected params
    const [hasInitialized, setHasInitialized] = useState(false);
    const [variablesTags, setVariablesTags] = useState(model.varTypes);
    const [variableBoundSets, setVariableBoundSets] = useState({});  // Store bound set selections
    const [enableBoundSets, setEnableBoundSets] = useState({});  // Track which variables have bound sets enabled
   
    useEffect(() => {
        // Get sets from model.setTypes instead of variable dependencies
        const setNames = model.setTypes ? Object.keys(model.setTypes) : [];
        setDisplaySets(setNames);
        
        // Get params from model.paramTypes instead of variable dependencies
        const paramNames = model.paramTypes ? Object.keys(model.paramTypes) : [];
        setDisplayParams(paramNames);
        
        // Only during initialization, also set the selected sets and params
        if (!hasInitialized && setNames.length > 0) {
          setSelectedSets(setNames);
          setSelectedParams(paramNames);
          setHasInitialized(true);
        }
        
    }, [model.setTypes, model.paramTypes, hasInitialized]);

    // Handles variable selection (checkbox clicked)
    const handleVarCheckboxChange = (variable) => {
        setSelectedVars(prevSelectedVars => {
            if (prevSelectedVars.includes(variable)) {
                return prevSelectedVars.filter(v => v !== variable);
            } else {
                return [...prevSelectedVars, variable];
            }
        });
    };

    // Handles set selection (checkbox clicked)
    const handleSetCheckboxChange = (set) => {
        setSelectedSets(prevSelected => {
            if (prevSelected.includes(set)) {
                return prevSelected.filter(s => s !== set);
            } else {
                return [...prevSelected, set];
            }
        });
    };

    // Handles parameter selection (checkbox clicked)
    const handleParamCheckboxChange = (param) => {
        setSelectedParams(prevSelected => {
            if (prevSelected.includes(param)) {
                return prevSelected.filter(p => p !== param);
            } else {
                return [...prevSelected, param];
            }
        });
    };

    // Handle enabling/disabling bound set selection for a variable
    const handleEnableBoundSet = (variableIdentifier) => {
        setEnableBoundSets(prev => ({
            ...prev,
            [variableIdentifier]: !prev[variableIdentifier]
        }));
        
        // Clear bound set when disabling
        if (enableBoundSets[variableIdentifier]) {
            setVariableBoundSets(prev => {
                const updated = { ...prev };
                delete updated[variableIdentifier];
                return updated;
            });
        }
    };

    // Handle radio button selection for bound sets
    const handleBoundSetChange = (variableIdentifier, setName) => {
        setVariableBoundSets(prev => ({
            ...prev,
            [variableIdentifier]: setName
        }));
    };

    // Save selected variables, sets, and parameters in context when navigating
    const handleContinue = () => {
        // Transform selectedVars from identifiers to VariableDTO objects
        const variablesOfInterestObjects = selectedVars.map(v => ({
            identifier: v.identifier,
            tags: variablesTags[v.identifier] || model.varTypes[v.identifier] || [],
            type: model.varTypes[v.identifier] || [],
            boundSet: enableBoundSets[v.identifier] ? variableBoundSets[v.identifier] : undefined
        }));
    
        // Transform selectedSets from strings to SetDefinitionDTO objects
        const inputSetsObjects = selectedSets.map(setName => ({
            name: setName,
            tags: model.setTypes?.[setName] || [],
            type: model.setTypes?.[setName] || []
        }));
    
        // Transform selectedParams from strings to ParameterDefinitionDTO objects
        const inputParamsObjects = selectedParams.map(paramName => ({
            name: paramName,
            type: model.paramTypes?.[paramName],
            tag: model.paramTypes?.[paramName]
        }));
    
        updateImageField("variablesModule", {
            variablesOfInterest: variablesOfInterestObjects,
            inputSets: inputSetsObjects,
            inputParams: inputParamsObjects
        });
    };

    // In your parent component where selectedVars is defined
    const handleTagValueChange = (e, varIdentifier, index) => {
        // Make a copy of the current state
        const updatedVariablesTags = { ...variablesTags };
        
        // Update the specific value at the specific index for the specific variable
        updatedVariablesTags[varIdentifier][index] = e.target.value;
        
        // Update the state
        setVariablesTags(updatedVariablesTags);
    };

    return (
        <div className="configure-variables-page">
            <h1 className="page-title">Configure Variables</h1>
            <div className="variables-layout">
                
                {/* Variables Section */}
                <div className="available-variables">
                    <h2>Available Variables</h2>
                    {Array.from(model.variables).length > 0 ? (
                        Array.from(model.variables).map((variable, index) => {
                            
                            return (
                            <Checkbox
                                key={index}
                                label={variable.identifier}
                                checked={selectedVars.includes(variable)}
                                onChange={() => handleVarCheckboxChange(variable)}
                            />
                        )})
                    ) : (
                        <p>No variables available.</p>
                    )}
                </div>
                
                {/* Sets & Parameters Section (Now from model.setTypes/paramTypes) */}
                <div className="involved-section">
                    <h2>Involved Sets</h2>
                    {displaySets.length > 0 ? (
                        displaySets.map((set, index) => (
                            <Checkbox
                                key={index}
                                label={set}
                                checked={selectedSets.includes(set)}
                                onChange={() => handleSetCheckboxChange(set)}
                            />
                        ))
                    ) : (
                        <p>No sets available.</p>
                    )}

                    <h2>Involved Parameters</h2>
                    {displayParams.length > 0 ? (
                        displayParams.map((param, index) => (
                            <Checkbox
                                key={index}
                                label={param}
                                checked={selectedParams.includes(param)}
                                onChange={() => handleParamCheckboxChange(param)}
                            />
                        ))
                    ) : (
                        <p>No parameters available.</p>
                    )}
                </div>
                <div className="tags-container">
                    <h2>Tag Variables' Output Tuple</h2>
                    {selectedVars.map((variable, key) => (
                        <div key={key} className="variable-config-item">
                            <TagConfigure
                                label="s"
                                variable={variable}
                                types={model.varTypes[variable.identifier]}
                                values={variablesTags[variable.identifier] || []}
                                onChange={(e, index) => handleTagValueChange(e, variable.identifier, index)}
                            />
                            
                            {/* Bound Set Selection */}
                            <div className="bound-set-selector">
                                <Checkbox
                                    label={`Select bound set for ${variable.identifier}`}
                                    checked={enableBoundSets[variable.identifier] || false}
                                    onChange={() => handleEnableBoundSet(variable.identifier)}
                                />
                                
                                {enableBoundSets[variable.identifier] && (
                                    <div className="bound-set-radio-group">
                                        {displaySets.map((setName, setIdx) => (
                                            <div key={setIdx} className="radio-option">
                                                <input
                                                    type="radio"
                                                    id={`${variable.identifier}-set-${setIdx}`}
                                                    name={`bound-set-${variable.identifier}`}
                                                    value={setName}
                                                    checked={variableBoundSets[variable.identifier] === setName}
                                                    onChange={() => handleBoundSetChange(variable.identifier, setName)}
                                                />
                                                <label htmlFor={`${variable.identifier}-set-${setIdx}`}>{setName}</label>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            
            <Link to="/configure-constraints" className="continue-button" onClick={handleContinue}>
                Continue
            </Link>
            <Link to="/" className="back-button">
                Back
            </Link>
        </div>
    );
};

export default ConfigureVariablesPage;