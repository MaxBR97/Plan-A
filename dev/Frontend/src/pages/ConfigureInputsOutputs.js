import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext.js";
import "./ConfigureInputsOutputs.css";
import Checkbox from "../reusableComponents/Checkbox.js";
import TagConfigure from "../reusableComponents/TagConfigure.js";

const ConfigureInputsOutputs = () => {
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

    // Initialize state from existing imageDTO values
    const [selectedVars, setSelectedVars] = useState(() => {
        if (image.variablesModule?.variablesOfInterest) {
            // Convert model.variables Set to Array and filter based on existing configuration
            return Array.from(model.variables).filter(v => 
                image.variablesModule.variablesOfInterest.some(vi => vi.identifier === v.identifier)
            );
        }
        return Array.from(model.variables || []);
    });

    const [displaySets, setDisplaySets] = useState([]);
    const [displayParams, setDisplayParams] = useState([]);
    
    // Initialize selected sets from existing configuration
    const [selectedSets, setSelectedSets] = useState(() => {
        return image.variablesModule?.inputSets?.map(s => s.name) || [];
    });

    // Initialize selected params from existing configuration
    const [selectedParams, setSelectedParams] = useState(() => {
        return image.variablesModule?.inputParams?.map(p => p.name) || [];
    });

    const [hasInitialized, setHasInitialized] = useState(false);
    const [variablesTags, setVariablesTags] = useState(() => {
        const initialTags = {};
        Array.from(model.variables || []).forEach(variable => {
            const varIdentifier = variable.identifier;
            const varModelTypes = model.varTypes[varIdentifier] || [];
            const configuredVariableOfInterest = image.variablesModule?.variablesOfInterest?.find(
                vConfig => vConfig.identifier === varIdentifier
            );

            if (configuredVariableOfInterest && configuredVariableOfInterest.tags) {
                if (configuredVariableOfInterest.tags.length === varModelTypes.length) {
                    initialTags[varIdentifier] = configuredVariableOfInterest.tags;
                } else {
                    initialTags[varIdentifier] = varModelTypes.map((_, index) => `${varIdentifier}_${index + 1}`);
                }
            } else {
                initialTags[varIdentifier] = varModelTypes.map((_, index) => `${varIdentifier}_${index + 1}`);
            }
        });
        return initialTags;
    });

    // Initialize bound sets from existing configuration
    const [variableBoundSets, setVariableBoundSets] = useState(() => {
        const boundSets = {};
        if (image.variablesModule?.variablesOfInterest) {
            image.variablesModule.variablesOfInterest.forEach(v => {
                if (v.boundSet) {
                    boundSets[v.identifier] = v.boundSet;
                }
            });
        }
        return boundSets;
    });

    useEffect(() => {
        // Set display sets and params from model types
        const setNames = model.setTypes ? Object.keys(model.setTypes) : [];
        setDisplaySets(setNames);
        
        const paramNames = model.paramTypes ? Object.keys(model.paramTypes) : [];
        setDisplayParams(paramNames);
        
        // Only initialize if not already done and there are sets/params
        if (!hasInitialized && setNames.length > 0) {
            // Only set if not already set from image configuration
            if (selectedSets.length === 0) {
                setSelectedSets(setNames);
            }
            if (selectedParams.length === 0) {
                setSelectedParams(paramNames);
            }
            setHasInitialized(true);
        }

    }, [model.setTypes, model.paramTypes, hasInitialized, selectedSets.length, selectedParams.length]);

    const handleVarCheckboxChange = (variable) => {
        setSelectedVars(prevSelectedVars => {
            if (prevSelectedVars.includes(variable)) {
                return prevSelectedVars.filter(v => v !== variable);
            } else {
                return [...prevSelectedVars, variable];
            }
        });
    };

    const handleSetCheckboxChange = (set) => {
        setSelectedSets(prevSelected => {
            if (prevSelected.includes(set)) {
                return prevSelected.filter(s => s !== set);
            } else {
                return [...prevSelected, set];
            }
        });
    };

    const handleParamCheckboxChange = (param) => {
        setSelectedParams(prevSelected => {
            if (prevSelected.includes(param)) {
                return prevSelected.filter(p => p !== param);
            } else {
                return [...prevSelected, param];
            }
        });
    };

    const handleBoundSetChange = (variableIdentifier, setName) => {
        setVariableBoundSets(prev => ({
            ...prev,
            [variableIdentifier]: setName
        }));
    };

    const handleTagValueChange = (e, varIdentifier, index) => {
        setVariablesTags(prev => {
            const updated = { ...prev };
            if (!updated[varIdentifier]) {
                updated[varIdentifier] = Array(model.varTypes[varIdentifier]?.length || 0).fill('');
            }
            updated[varIdentifier][index] = e.target.value;
            return updated;
        });
    };

    const handleContinue = () => {
        const variablesOfInterestObjects = selectedVars.map(v => {
            const boundSetValue = variableBoundSets[v.identifier];
            // Create base variable object without boundSet
            const variableObject = {
                identifier: v.identifier,
                tags: variablesTags[v.identifier] || Array(model.varTypes[v.identifier]?.length || 0).fill(''),
                type: model.varTypes[v.identifier] || []
            };

            // Only add boundSet if it's a real set (either a set named 'none' or any other set)
            if (boundSetValue && (boundSetValue !== 'none' || displaySets.includes('none'))) {
                variableObject.boundSet = boundSetValue;
            }

            return variableObject;
        });
    
        const inputSetsObjects = selectedSets.map(setName => ({
            name: setName,
            tags: model.setTypes?.[setName] || [],
            type: model.setTypes?.[setName] || []
        }));
    
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
        console.log("CONTINUE CLICK, cur image:", image);
    };

    return (
        <div className="configure-variables-page">
            <h1 className="page-title">Configure Problem Domain</h1>
            <p className="page-description">
                Define the inputs and outputs of your optimization problem. Select which variables you want to track as outputs,
                and specify the sets and parameters that will serve as inputs. You can also customize how the output data will be displayed.
            </p>
            <div className="variables-layout">
                {/* Left side - Outputs */}
                <div className="available-variables">
                    <h2>Outputs</h2>
                    {Array.from(model.variables).length > 0 ? (
                        Array.from(model.variables).map((variable, index) => (
                            <Checkbox
                                key={index}
                                label={variable.identifier}
                                checked={selectedVars.includes(variable)}
                                onChange={() => handleVarCheckboxChange(variable)}
                            />
                        ))
                    ) : (
                        <p>No variables available.</p>
                    )}
                </div>

                {/* Right side - Inputs */}
                <div className="involved-section">
                    <h2>Inputs</h2>
                    <h3>Sets</h3>
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

                    <h3>Parameters</h3>
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
            </div>

            {/* Bottom section - Tag Variables' Output Tuple */}
            <div className="tags-container">
                <h2>Tag Variables' Output Tuple</h2>
                {selectedVars.map((variable, key) => (
                    <div key={key} className="variable-config-item">
                        <div className="variable-name">{variable.identifier}</div>
                        <div className="output-columns-label">Name each output column</div>
                        <div className="tag-inputs-row">
                            {model.varTypes[variable.identifier]?.map((type, index) => (
                                <div key={index} className="tag-input-group">
                                    <div className="tag-type-label" title={type}>{type}</div>
                                    <input
                                        type="text"
                                        value={variablesTags[variable.identifier]?.[index]}
                                        onChange={(e) => handleTagValueChange(e, variable.identifier, index)}
                                        // placeholder={`${variable.identifier}_${index + 1}`}
                                    />
                                </div>
                            ))}
                        </div>
                        <div className="bound-set-selector">
                            <div className="bound-set-label">Choose bound set</div>
                            <select
                                value={variableBoundSets[variable.identifier] || "none"}
                                onChange={(e) => handleBoundSetChange(variable.identifier, e.target.value)}
                            >
                                <option value="none">None</option>
                                {displaySets.map((setName) => (
                                    <option key={setName} value={setName}>
                                        {setName}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                ))}
            </div>
            
            <Link to="/configuration-menu" className="continue-button" onClick={handleContinue}>
                Continue
            </Link>
            {/* <Link to="/configuration-menu" className="back-button">
                Back
            </Link> */}
        </div>
    );
};

export default ConfigureInputsOutputs;