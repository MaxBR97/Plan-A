import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigurePreferencesPage.css';
import Checkbox from '../reusableComponents/Checkbox';

const ConfigurePreferencesPage = () => {
    const navigate = useNavigate();
    const {
        image,
        model,
        solutionResponse,
        updateImage,
        updateImageField,
        updateImageFieldWithCallBack,
        updateModel,
        updateSolutionResponse,
        initialImageState
    } = useZPL();

    const [availablePreferences, setAvailablePreferences] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);
    const bannedSets = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []))];
    const bannedParams = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []))];
    const [allModules, setAllModules] = useState(Array.from(image.preferenceModules) || []);
    const [involvedSets, setInvolvedSets] = useState([]);
    const [involvedParams, setInvolvedParams] = useState([]);
    const [sendRequestAndContinue, setSendRequestAndContinue] = useState(false);
    

    const patchConfigurations = async () => {
        const patchRequestBody = {
          imageId : image.imageId,
          image
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
            try {
                const errorData = await patchResponse.json();
                throw new Error(`PATCH request failed! Status: ${patchResponse.status}. Details: ${JSON.stringify(errorData)}`);
            } catch (jsonError) {
                const errorText = await patchResponse.text();
                throw new Error(`PATCH request failed! Status: ${patchResponse.status} ${patchResponse.statusText}. Details: ${errorText}`);
            }
          }
          
          console.log("✅ PATCH request successful!");
          return true;
      } catch (error) {
          console.error("Error sending PATCH request:", error);
          //setErrorMessage(`Failed to update image metadata: ${error.message}`);
          return false; // Stop execution if PATCH fails
      }
    }

    
    useEffect(() => {   
        if(sendRequestAndContinue)
            updateImageField("preferenceModules", allModules)
    }, [sendRequestAndContinue]); // Include 'image' as a dependency

    useEffect(() => {
        if(sendRequestAndContinue){
            const fetchData = async () => {
                
                const success = await patchConfigurations();
                if (success) {
                    navigate("/solution-preview");
                }
            };
            setSendRequestAndContinue(false)
            fetchData(); 
        }
        
    }, [image]); // Include 'image' as a dependency

    const handleContinue = async () => {
        setSendRequestAndContinue(true)
    };
    
    useEffect(() => {
        setAvailablePreferences(Array.from(model.preferences).filter((c) => 
            allModules.every((module) => 
                !module.preferences.some(preference => 
                    typeof preference === 'string' 
                        ? preference === c.identifier 
                        : preference.identifier === c.identifier
                )
            )
        ));
        if(selectedModuleIndex != null && allModules.length > 0){
            setInvolvedSets(
                Array.from(
                  new Set(
                    Array.from(model.preferences)
                      .flatMap((preference) => 
                        Array.from(allModules[selectedModuleIndex].preferences).includes(preference.identifier) 
                          ? Array.from(preference.dep?.setDependencies || []).filter(setDep => {
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
                    Array.from(model.preferences)
                      .flatMap((preference) => 
                        Array.from(allModules[selectedModuleIndex].preferences).includes(preference.identifier) 
                          ? Array.from(preference.dep?.paramDependencies || []).filter(paramDep => {
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
    }, [model.preferences, allModules, selectedModuleIndex]);

    const addPreferenceModule = () => {
        if (moduleName.trim() !== '') {
            setAllModules(
            [ ...allModules,
                {   
                    moduleName: moduleName,
                    description: "", 
                    preferences: [], 
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

    

    const addPreferenceToModule = (preference) => {
        if (selectedModuleIndex === null) {
            alert('Please select a module first!');
            return;
        }

        setAllModules(
            allModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    if (!module.preferences.some(c => typeof c === 'string' ? c === preference.identifier : c.identifier === preference.identifier)) {
                        // Get sets and params from this preference
                        const preferenceSets = preference.dep?.setDependencies || [];
                        const preferenceParams = preference.dep?.paramDependencies || [];
                        
                        // Filter out banned sets and params
                        const newSets = preferenceSets.filter(set => !bannedSets.includes(set));
                        const newParams = preferenceParams.filter(param => !bannedParams.includes(param));
                        
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
                            preferences: [...module.preferences, preference.identifier], // Just store the identifier
                            inputSets: updatedInputSets,
                            inputParams: updatedInputParams
                        };
                    }
                }
                return module;
            })
        );
        
        setAvailablePreferences((prev) => {
            const filteredPreferences = prev.filter((c) => c.identifier !== preference.identifier);
            const uniquePreferences = Array.from(
                new Map(filteredPreferences.map(item => [item.identifier, item])).values()
            );
            
            return uniquePreferences;
        });
    };

    const removePreferenceFromModule = (preference) => {
        if (selectedModuleIndex === null) return;

        const preferenceId = typeof preference === 'string' ? preference : preference.identifier;

        setAllModules(
            allModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    const newPreferences = module.preferences.filter(c => 
                        typeof c === 'string' ? c !== preferenceId : c.identifier !== preferenceId
                    );
                    
                    // Recalculate all needed sets and params from remaining preferences
                    const remainingPreferenceObjects = newPreferences.map(c => {
                        if (typeof c === 'string') {
                            // Find the full preference object from available preferences or model
                            return availablePreferences.find(ac => ac.identifier === c) || 
                                   Array.from(model.preferences).find(mc => mc.identifier === c);
                        }
                        return c;
                    }).filter(Boolean); // Remove any undefined values
                    
                    const allSetDependencies = new Set();
                    const allParamDependencies = new Set();
                    
                    // Collect all dependencies from remaining preferences
                    remainingPreferenceObjects.forEach(c => {
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
                        preferences: newPreferences,
                        inputSets: updatedInputSets,
                        inputParams: updatedInputParams
                    };
                }
                return module;
            })
        );
        
        setAvailablePreferences((prev) => prev.some(c => c.identifier === preferenceId) ? prev : [...prev, preference]);
    };

    const deleteModule = (index) => {
        // Get all preferences from the module being deleted
        const modulePreferences = allModules[index].preferences;
        
        // Add them back to available preferences
        modulePreferences.forEach(preference => {
            const preferenceObj = typeof preference === 'string' 
                ? Array.from(model.preferences).find(c => c.identifier === preference)
                : preference;
                
            if (preferenceObj) {
                setAvailablePreferences(prev => 
                    prev.some(c => c.identifier === preferenceObj.identifier) 
                        ? prev 
                        : [...prev, preferenceObj]
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
    
    const handleToggleInvolvedParam = (paramName, mode) => {
        
        setAllModules(
            allModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    let updatedInputParams = module.inputParams ? module.inputParams.filter(p => p.name !== paramName) : [];
                    let updatedCostParams = module.costParams ? module.costParams.filter(p => p.name !== paramName) : [];
                    
                    if (mode === "raw") {
                        const newParamDTO = {
                            name: paramName,
                            tag: model.paramTypes?.[paramName],
                            type: model.paramTypes?.[paramName],
                        };
                        updatedInputParams = [...updatedInputParams, newParamDTO];
                    } else if (mode === "relative") {
                        const newParamDTO = {
                            name: paramName,
                            tag: model.paramTypes?.[paramName],
                            type: model.paramTypes?.[paramName],
                        };
                        updatedInputParams = [...updatedInputParams, newParamDTO];
                        updatedCostParams = [...updatedCostParams, newParamDTO];
                    }
    
                    return { 
                        ...module, 
                        inputParams: updatedInputParams, 
                        costParams: updatedCostParams 
                    };
                }
                return module;
            })
        );
    };
    
    


    // const handleToggleInvolvedParamRelative = (paramName) => {
    //     setAllModules(
    //         allModules.map((module, idx) => {
    //             if (idx === selectedModuleIndex) {
    //                 // Ensure costParams exists, defaulting to an empty array if undefined
    //                 const currentCostParams = module.costParams || [];
                    
    //                 // Check if the param is already in inputParams
    //                 const isParamIncluded = currentCostParams.some(p => p.name === paramName);
    //                 let updatedParams;
                    
    //                 if (isParamIncluded) {
    //                     // Remove if included
    //                     updatedParams = currentCostParams.filter(p => p.name !== paramName);
    //                 } else {
    //                     // Add if not included
    //                     const newParamDTO = {
    //                         name: paramName,
    //                         tag: model.paramTypes?.[paramName],
    //                         type: model.paramTypes?.[paramName]
    //                     };
    //                     updatedParams = [...currentCostParams, newParamDTO];
    //                 }
    
    //                 return { ...module, costParams: updatedParams };
    //             }
    //             return module;
    //         })
    //     );
    // };
    
    const handleBack = () => {
        updateImageField("preferenceModules", allModules)
        navigate('/');
    };

    return (
        <div className="configure-preferences-page">
            <h1 className="page-title">Configure Preference Modules</h1>

            <div className="preferences-layout">
                {/* Preference Modules Section */}
                <div className="preference-modules">
                    <h2>Preference Modules</h2>
                    <input
                        type="text"
                        placeholder="Module Name"
                        value={moduleName}
                        onChange={(e) => setModuleName(e.target.value)}
                    />
                    <button onClick={addPreferenceModule}>Add Preference Module</button>
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

                {/* Define Preference Module Section */}
                <div className="define-preference-module">
                    <h2>Define Preference Module</h2>
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
                            <p>This module's preferences:</p>
                            <hr />
                            <div className="module-drop-area">
                                {allModules[selectedModuleIndex]?.preferences?.length > 0 ? (
                                    allModules[selectedModuleIndex].preferences.map((c, i) => (
                                        <div 
                                            key={i} 
                                            className="dropped-preference preference-box"
                                            onClick={() => removePreferenceFromModule(c)}
                                        >
                                            {c}
                                        </div>
                                    ))
                                ) : (
                                    <p>No preferences added</p>
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
                            <div className="flex flex-col gap-6">
            {involvedParams.map((param, i) => (
                <div key={i} className="border p-4 rounded-lg shadow-md flex flex-col items-center bg-gray-100">
                    <h3 className="font-semibold text-lg mb-2">{param}</h3>
                    <div className="flex gap-6">
                    <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                name={param}
                                checked={allModules[selectedModuleIndex]?.inputParams.every(inputParams => inputParams.name !== param)} 
                                onChange={() => handleToggleInvolvedParam(param,"none")}
                            />
                            None
                        </label>
                        <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                name={param}
                                checked={allModules[selectedModuleIndex]?.inputParams.some(inputParams => inputParams.name == param) && allModules[selectedModuleIndex]?.costParams?.every(costParam => costParam.name !== param)} 
                                onChange={() => handleToggleInvolvedParam(param,"raw")}
                            />
                            Raw
                        </label>
                        <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                name={param}
                                checked={allModules[selectedModuleIndex]?.costParams?.some(costParam => costParam.name === param)} 
                                onChange={() => handleToggleInvolvedParam(param, "relative")}
                            />
                            Relative
                        </label>
                    </div>
                </div>
            ))}
        </div>

                        </>
                    )}
                </div>

                {/* Available Preferences Section */}
                <div className="available-preferences">
                    <h2>Available Preferences</h2>
                    {availablePreferences.length > 0 ? (
                        availablePreferences.map((preference, idx) => (
                            <div key={idx} className="preference-item-container">
                                <button className="preference-item" onClick={() => addPreferenceToModule(preference)}>
                                    {preference.identifier}
                                </button>
                            </div>
                        ))
                    ) : (
                        <p>No preferences available</p>
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

export default ConfigurePreferencesPage;
