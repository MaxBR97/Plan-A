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
        updateModel,
        updateSolutionResponse,
        initialImageState
      } = useZPL();

    const [availablePreferences, setAvailablePreferences] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);
    const bannedSets = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []))];
    const bannedParams = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []))];
    console.log(image.preferenceModules)
    console.log(bannedParams)
    

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
      
      const handleContinue = () => {
        if(patchConfigurations())
            navigate("/solution-preview")
      }

    // Initialize available preferences dynamically from JSON
    useEffect(() => {
        setAvailablePreferences(Array.from(model.preferences).filter((c) => image.preferenceModules.every((module) => !module.preferences.includes(c))));
    }, [model.preferences]);

    const addPreferenceModule = () => {
        if (moduleName.trim() !== '') {
            updateImageField("preferenceModules", 
            [ ... image.preferenceModules,
                {   
                    name: moduleName, 
                    description: "", 
                    preferences: [], 
                    involvedSets: [], 
                    involvedParams: [] , 
                    inputSets:[], 
                    inputParams:[]
                }
            ]);
            setModuleName('');
        }
    };

    const updateModuleDescription = (newDescription) => {
        updateImageField("preferenceModules",
            image.preferenceModules.map((module, idx) =>
                idx === selectedModuleIndex ? { ...module, description: newDescription } : module
            )
        )
        
    };

    const addPreferenceToModule = (preference) => {
        if (selectedModuleIndex === null) {
            alert('Please select a module first!');
            return;
        }

        updateImageField("preferenceModules",
            image.preferenceModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    if (!module.preferences.some(c => c.identifier === preference.identifier)) {
                        return {
                            ...module,
                            preferences: [...module.preferences, preference],
                            involvedSets: [...new Set([...module.involvedSets, ...(preference.dep?.setDependencies || [])])].filter((set) => !bannedSets.includes(set)),
                            involvedParams: [...new Set([...module.involvedParams, ...(preference.dep?.paramDependencies || [])])].filter((param) => !bannedParams.includes(param))
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

        updateImageField("preferenceModules",
            image.preferenceModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    const newPreferences = module.preferences.filter(c => c.identifier !== preference.identifier);

                    const remainingSets = new Set();
                    const remainingParams = new Set();
                    
                    const allSetDependencies = new Set(
                        newPreferences.flatMap(c => c.dep?.setDependencies || [])
                    );
                    const allParamDependencies = new Set(
                        newPreferences.flatMap(c => c.dep?.paramDependencies || [])
                    );

                    const filteredSets = [...allSetDependencies].filter(set => !bannedSets.includes(set));
                    const filteredParams = [...allParamDependencies].filter(param => !bannedParams.includes(param));

                    filteredSets.forEach(set => remainingSets.add(set));
                    filteredParams.forEach(param => remainingParams.add(param));

                    return {
                        ...module,
                        preferences: newPreferences,
                        involvedSets: [...remainingSets],
                        involvedParams: [...remainingParams]
                    };
                }
                return module;
            })
        );
        
        setAvailablePreferences((prev) => prev.includes(preference) ? prev : [...prev, preference]);
    };

    const deleteModule = (index) => {
        
        image.preferenceModules[index].preferences.forEach((preference) => removePreferenceFromModule(preference))
        updateImageField("preferenceModules", image.preferenceModules.filter((_, i) => i !== index));
    
        // Reset selection if the deleted module was selected
        if (selectedModuleIndex === index) {
            setSelectedModuleIndex(null);
        } else if (selectedModuleIndex > index) {
            setSelectedModuleIndex(selectedModuleIndex - 1); // Adjust index if needed
        }
    };

    //console.log(modules)
    const handleToggleInvolvedSet = (setName) => {
        updateImageField("preferenceModules", 
            image.preferenceModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    // Check if the set is already in inputSets
                    const isSetIncluded = module.inputSets.includes(setName);
                    const updatedInputSets = isSetIncluded
                        ? module.inputSets.filter((s) => s !== setName) // Remove if already included
                        : [...module.inputSets, setName]; // Add if not included
                    
                    return { ...module, inputSets: updatedInputSets };
                }
                return module;
            })
        );
    };
    
    const handleToggleInvolvedParam = (paramName) => {
        
        updateImageField("preferenceModules",
            image.preferenceModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    // Check if the param is already in involvedParams
                    const isParamIncluded = module.inputParams.includes(paramName);
                    const updatedParams = isParamIncluded
                        ? module.inputParams.filter((p) => p !== paramName) // Remove if included
                        : [...module.inputParams, paramName]; // Add if not included
    
                    return { ...module, inputParams: updatedParams };
                }
                return module;
            })
        );
    };
    
    
    

    return (
        <div className="configure-preferences-page">
            <h1 className="page-title">Configure Preference Modules</h1>

            <div className="preferences-layout">
                {/* preference Modules Section */}
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
                        {image.preferenceModules.map((module, index) => (
                            <div key={index} className="module-item-container">
                                <button 
                                    className={`module-item ${selectedModuleIndex === index ? 'selected' : ''}`} 
                                    onClick={() => setSelectedModuleIndex(index)}
                                >
                                    {module.name}
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

                {/* Define preference Module Section */}
                <div className="define-preference-module">
                    <h2>Define Preference Module</h2>
                    {selectedModuleIndex === null ? (
                        <p>Select a module</p>
                    ) : (
                        <>
                            <h3>{image.preferenceModules[selectedModuleIndex]?.name || 'Unnamed Module'}</h3>
                            <label>Description:</label>
                            <hr />
                            <textarea
                                value={image.preferenceModules[selectedModuleIndex]?.description || ""}
                                onChange={(e) => updateModuleDescription(e.target.value)}
                                placeholder="Enter module description..."
                                style={{ resize: "none", width: "100%", height: "80px" }}
                            />
                            <p>This module's preferences:</p>
                            <hr />
                            <div className="module-drop-area">
                                {image.preferenceModules[selectedModuleIndex]?.preferences?.length > 0 ? (
                                    image.preferenceModules[selectedModuleIndex].preferences.map((c, i) => (
                                        <div 
                                            key={i} 
                                            className="dropped-preference preference-box"
                                            onClick={() => removePreferenceFromModule(c)}
                                        >
                                            {c.identifier}
                                        </div>
                                    ))
                                ) : (
                                    <p>No preferences added</p>
                                )}
                            </div>

                            <h3>Select input Sets:</h3>
                            <div>
                                {image.preferenceModules[selectedModuleIndex]?.involvedSets.map((set, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={image.preferenceModules[selectedModuleIndex]?.inputSets.includes(set)} 
                                            onChange={() => handleToggleInvolvedSet(set)}
                                        /> {set}
                                    </div>
                                ))}
                            </div>

                            <h3>Select input Parameters:</h3>
                            <div>
                                {image.preferenceModules[selectedModuleIndex]?.involvedParams.map((param, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={image.preferenceModules[selectedModuleIndex]?.inputParams.includes(param)} 
                                            onChange={() => handleToggleInvolvedParam(param)}
                                        /> {param}
                                    </div>
                                ))}
                            </div>

                        </>
                    )}
                </div>

                {/* Available preferences Section */}
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

            <Link to="/" className="back-button">
                Back
            </Link>
        </div>
    );
};

export default ConfigurePreferencesPage;
