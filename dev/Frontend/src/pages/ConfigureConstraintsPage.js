import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';
import Checkbox from '../reusableComponents/Checkbox';

const ConfigureConstraintsPage = () => {
    const navigate = useNavigate();
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

    const [availableConstraints, setAvailableConstraints] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);
    const bannedSets = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []))];
    const bannedParams = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []))];
    const [allModules, setAllModules] = useState(Array.from(image.constraintModules) || []);
    const [involvedSets, setInvolvedSets] = useState([]);
    const [involvedParams, setInvolvedParams] = useState([]);
    
    useEffect(() => {
        setAvailableConstraints(Array.from(model.constraints).filter((c) => 
            allModules.every((module) => 
                !module.constraints.some(constraint => 
                    typeof constraint === 'string' 
                        ? constraint === c.identifier 
                        : constraint.identifier === c.identifier
                )
            )
        ));
        if(selectedModuleIndex != null && allModules.length > 0){
            setInvolvedSets(
                Array.from(
                  new Set(
                    Array.from(model.constraints)
                      .flatMap((constraint) => 
                        Array.from(allModules[selectedModuleIndex].constraints).includes(constraint.identifier) 
                          ? Array.from(constraint.dep?.setDependencies || []).filter(setDep => {
                              // Check if this setDep doesn't appear in any variable's setDependencies
                              return !Array.from(model.variables || []).some(variable => 
                                variable.dep?.setDependencies?.includes(setDep) || 
                                (Array.isArray(variable.dep?.setDependencies) && 
                                 variable.dep.setDependencies.includes(setDep))
                              );
                            })
                          : []
                      )
                  )
                )
              );
              setInvolvedParams(
                Array.from(
                  new Set(
                    Array.from(model.constraints)
                      .flatMap((constraint) => 
                        Array.from(allModules[selectedModuleIndex].constraints).includes(constraint.identifier) 
                          ? Array.from(constraint.dep?.paramDependencies || []).filter(paramDep => {
                              // Check if this paramDep doesn't appear in any variable's paramDependencies
                              return !Array.from(model.variables || []).some(variable => 
                                variable.dep?.paramDependencies?.includes(paramDep) || 
                                (Array.isArray(variable.dep?.paramDependencies) && 
                                 variable.dep.paramDependencies.includes(paramDep))
                              );
                            })
                          : []
                      )
                  )
                )
              );
            }
    }, [model.constraints, allModules, selectedModuleIndex]);

    const addConstraintModule = () => {
        if (moduleName.trim() !== '') {
            setAllModules(
            [ ...allModules,
                {   
                    moduleName: moduleName,
                    description: "", 
                    constraints: [], 
                    inputSets: [],
                    inputParams: []
                }
            ]);
            setModuleName('');
        }
    };

    const updateModuleDescription = (newDescription) => {
        setAllModules(
            allModules.map((module, idx) =>
                idx === selectedModuleIndex ? { ...module, description: newDescription } : module
            )
        );
    };

    const addConstraintToModule = (constraint) => {
        if (selectedModuleIndex === null) {
            alert('Please select a module first!');
            return;
        }

        setAllModules(
            allModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    if (!module.constraints.some(c => typeof c === 'string' ? c === constraint.identifier : c.identifier === constraint.identifier)) {
                        // Get sets and params from this constraint
                        const constraintSets = constraint.dep?.setDependencies || [];
                        const constraintParams = constraint.dep?.paramDependencies || [];
                        
                        // Filter out banned sets and params
                        const newSets = constraintSets.filter(set => !bannedSets.includes(set));
                        const newParams = constraintParams.filter(param => !bannedParams.includes(param));
                        
                        // Convert sets to SetDefinitionDTO format
                        const newSetDTOs = newSets.map(setName => ({
                            name: setName,
                            tags: model.setTypes?.[setName] || [],
                            type: model.setTypes?.[setName] || []
                        }));
                        
                        // Convert params to ParameterDefinitionDTO format
                        const newParamDTOs = newParams.map(paramName => ({
                            name: paramName,
                            tag: model.paramTypes?.[paramName] ,
                            type: model.paramTypes?.[paramName] 
                        }));
                        
                        // Add new sets and params without duplicates
                        const updatedInputSets = [
                            ...module.inputSets,
                            ...newSetDTOs.filter(newSet => 
                                !module.inputSets.some(existingSet => 
                                    existingSet.name === newSet.name
                                )
                            )
                        ];
                        
                        const updatedInputParams = [
                            ...module.inputParams,
                            ...newParamDTOs.filter(newParam => 
                                !module.inputParams.some(existingParam => 
                                    existingParam.name === newParam.name
                                )
                            )
                        ];
                        
                        return {
                            ...module,
                            constraints: [...module.constraints, constraint.identifier], // Just store the identifier
                            inputSets: updatedInputSets,
                            inputParams: updatedInputParams
                        };
                    }
                }
                return module;
            })
        );
        
        setAvailableConstraints((prev) => {
            const filteredConstraints = prev.filter((c) => c.identifier !== constraint.identifier);
            const uniqueConstraints = Array.from(
                new Map(filteredConstraints.map(item => [item.identifier, item])).values()
            );
            
            return uniqueConstraints;
        });
    };

    const removeConstraintFromModule = (constraint) => {
        if (selectedModuleIndex === null) return;

        const constraintId = typeof constraint === 'string' ? constraint : constraint.identifier;

        setAllModules(
            allModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    const newConstraints = module.constraints.filter(c => 
                        typeof c === 'string' ? c !== constraintId : c.identifier !== constraintId
                    );
                    
                    // Recalculate all needed sets and params from remaining constraints
                    const remainingConstraintObjects = newConstraints.map(c => {
                        if (typeof c === 'string') {
                            // Find the full constraint object from available constraints or model
                            return availableConstraints.find(ac => ac.identifier === c) || 
                                   Array.from(model.constraints).find(mc => mc.identifier === c);
                        }
                        return c;
                    }).filter(Boolean); // Remove any undefined values
                    
                    const allSetDependencies = new Set();
                    const allParamDependencies = new Set();
                    
                    // Collect all dependencies from remaining constraints
                    remainingConstraintObjects.forEach(c => {
                        (c.dep?.setDependencies || [])
                            .filter(set => !bannedSets.includes(set))
                            .forEach(set => allSetDependencies.add(set));
                            
                        (c.dep?.paramDependencies || [])
                            .filter(param => !bannedParams.includes(param))
                            .forEach(param => allParamDependencies.add(param));
                    });
                    
                    // Filter existing DTOs to only keep relevant ones
                    const updatedInputSets = module.inputSets.filter(
                        setDTO => allSetDependencies.has(setDTO.name)
                    );
                    
                    const updatedInputParams = module.inputParams.filter(
                        paramDTO => allParamDependencies.has(paramDTO.name)
                    );
                    
                    return {
                        ...module,
                        constraints: newConstraints,
                        inputSets: updatedInputSets,
                        inputParams: updatedInputParams
                    };
                }
                return module;
            })
        );
        
        setAvailableConstraints((prev) => prev.some(c => c.identifier === constraintId) ? prev : [...prev, constraint]);
    };

    const deleteModule = (index) => {
        // Get all constraints from the module being deleted
        const moduleConstraints = allModules[index].constraints;
        
        // Add them back to available constraints
        moduleConstraints.forEach(constraint => {
            const constraintObj = typeof constraint === 'string' 
                ? Array.from(model.constraints).find(c => c.identifier === constraint)
                : constraint;
                
            if (constraintObj) {
                setAvailableConstraints(prev => 
                    prev.some(c => c.identifier === constraintObj.identifier) 
                        ? prev 
                        : [...prev, constraintObj]
                );
            }
        });
        
        // Remove the module
        setAllModules(allModules.filter((_, i) => i !== index));
    
        // Reset selection if the deleted module was selected
        if (selectedModuleIndex === index) {
            setSelectedModuleIndex(null);
        } else if (selectedModuleIndex > index) {
            setSelectedModuleIndex(selectedModuleIndex - 1); // Adjust index if needed
        }
    };

    const handleToggleInvolvedSet = (setName) => {
        setAllModules(
            allModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    // Check if the set is already in inputSets
                    const isSetIncluded = module.inputSets.some(s => s.name === setName);
                    let updatedInputSets;
                    
                    if (isSetIncluded) {
                        // Remove if already included
                        updatedInputSets = module.inputSets.filter(s => s.name !== setName);
                    } else {
                        // Add if not included
                        const newSetDTO = {
                            name: setName,
                            tags: model.setTypes?.[setName] || [],
                            type: model.setTypes?.[setName] || []
                        };
                        updatedInputSets = [...module.inputSets, newSetDTO];
                    }
                    
                    return { ...module, inputSets: updatedInputSets };
                }
                return module;
            })
        );
    };
    
    const handleToggleInvolvedParam = (paramName) => {
        setAllModules(
            allModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    // Check if the param is already in inputParams
                    const isParamIncluded = module.inputParams.some(p => p.name === paramName);
                    let updatedParams;
                    
                    if (isParamIncluded) {
                        // Remove if included
                        updatedParams = module.inputParams.filter(p => p.name !== paramName);
                    } else {
                        // Add if not included
                        const newParamDTO = {
                            name: paramName,
                            tag: model.paramTypes?.[paramName] ,
                            type: model.paramTypes?.[paramName] 
                        };
                        updatedParams = [...module.inputParams, newParamDTO];
                    }
    
                    return { ...module, inputParams: updatedParams };
                }
                return module;
            })
        );
    };

    const saveCurrentState = () => {
        updateImageField("constraintModules", allModules)
    }
    
    const handleContinue = () => {
        console.log("Leaving constraint config with: ", allModules);
        saveCurrentState();
        navigate('/configure-preferences');
    };
    
    const handleBack = () => {
        console.log("Leaving constraint config with: ", allModules);
        saveCurrentState();
        navigate('/');
    };

    return (
        <div className="configure-constraints-page">
            <h1 className="page-title">Configure Constraint Modules</h1>

            <div className="constraints-layout">
                {/* Constraint Modules Section */}
                <div className="constraint-modules">
                    <h2>Constraint Modules</h2>
                    <input
                        type="text"
                        placeholder="Module Name"
                        value={moduleName}
                        onChange={(e) => setModuleName(e.target.value)}
                    />
                    <button onClick={addConstraintModule}>Add Constraint Module</button>
                    <div className="module-list">
                        {allModules.map((module, index) => (
                            <div key={index} className="module-item-container">
                                <button 
                                    className={`module-item ${selectedModuleIndex === index ? 'selected' : ''}`} 
                                    onClick={() => setSelectedModuleIndex(index)}
                                >
                                    {module.moduleName}
                                </button>
                                <button 
                                    className="delete-module-button"
                                    onClick={(e) => {
                                        e.stopPropagation(); // Prevent selecting the module when clicking delete
                                        deleteModule(index);
                                    }}
                                >
                                    ❌
                                </button>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Define Constraint Module Section */}
                <div className="define-constraint-module">
                    <h2>Define Constraint Module</h2>
                    {selectedModuleIndex === null ? (
                        <p>Select a module</p>
                    ) : (
                        <>
                            <h3>{allModules[selectedModuleIndex]?.name || 'Unnamed Module'}</h3>
                            <label>Description:</label>
                            <hr />
                            <textarea
                                value={allModules[selectedModuleIndex]?.description || ""}
                                onChange={(e) => updateModuleDescription(e.target.value)}
                                placeholder="Enter module description..."
                                style={{ resize: "none", width: "100%", height: "80px" }}
                            />
                            <p>This module's constraints:</p>
                            <hr />
                            <div className="module-drop-area">
                                {allModules[selectedModuleIndex]?.constraints?.length > 0 ? (
                                    allModules[selectedModuleIndex].constraints.map((c, i) => (
                                        <div 
                                            key={i} 
                                            className="dropped-constraint constraint-box"
                                            onClick={() => removeConstraintFromModule(c)}
                                        >
                                            {c}
                                        </div>
                                    ))
                                ) : (
                                    <p>No constraints added</p>
                                )}
                            </div>

                            <h3>Select input Sets:</h3>
                            <div>
                                {involvedSets.map((set, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={allModules[selectedModuleIndex]?.inputSets.some(inputSet => inputSet.name === set)} 
                                            onChange={() => handleToggleInvolvedSet(set)}
                                        /> {set}
                                    </div>
                                ))}
                            </div>

                            <h3>Select input Parameters:</h3>
                            <div>
                                {involvedParams.map((param, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={allModules[selectedModuleIndex]?.inputParams.some(inputParams => inputParams.name === param)} 
                                            onChange={() => handleToggleInvolvedParam(param)}
                                        /> {param}
                                    </div>
                                ))}
                            </div>

                        </>
                    )}
                </div>

                {/* Available Constraints Section */}
                <div className="available-constraints">
                    <h2>Available Constraints</h2>
                    {availableConstraints.length > 0 ? (
                        availableConstraints.map((constraint, idx) => (
                            <div key={idx} className="constraint-item-container">
                                <button className="constraint-item" onClick={() => addConstraintToModule(constraint)}>
                                    {constraint.identifier}
                                </button>
                            </div>
                        ))
                    ) : (
                        <p>No constraints available</p>
                    )}
                </div>
            </div>

            <button
    className="continue-button"
    onClick={handleContinue}
>
    Continue
</button>

<button
    className="back-button"
    onClick={handleBack}
>
    Back
</button>
        </div>
    );
};

export default ConfigureConstraintsPage;
