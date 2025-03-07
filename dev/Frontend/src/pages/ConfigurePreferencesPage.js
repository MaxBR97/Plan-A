import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';
import Checkbox from '../reusableComponents/Checkbox';

const ConfigurePreferencesPage = () => {
    const navigate = useNavigate();

    // Fetch preferences & modules from ZPL context
    const { preferences: jsonPreferences = [], preferenceModules = [], setPreferenceModules = () => {} , variables} = useZPL();

    // Local states
    const [availablePreferences, setAvailablePreferences] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);
    const bannedSets = [...new Set(variables.flatMap(v => v.dep?.setDependencies || []))];
    const bannedParams = [...new Set(variables.flatMap(v => v.dep?.paramDependencies || []))];
    console.log(preferenceModules)
    console.log(bannedParams)

    // Initialize available preferences dynamically from JSON
useEffect(() => {
        setAvailablePreferences(jsonPreferences.filter((p) => preferenceModules.every((module) => !module.preferences.includes(p))));
    }, [jsonPreferences]);
    
    const addPreferenceModule = () => {
        if (moduleName.trim() !== '') {
            setPreferenceModules((prevModules) => [
                ...prevModules,
                { name: moduleName, description: "", preferences: [], involvedSets: [], involvedParams: [], inputSets:[], inputParams:[] }
            ]);
            setModuleName('');
        }
    };

    // Update module description
    const updateModuleDescription = (newDescription) => {
        setPreferenceModules((prevModules) =>
            prevModules.map((module, idx) =>
                idx === selectedModuleIndex ? { ...module, description: newDescription } : module
            )
        );
    };

    // Add preference to selected module
    const addPreferenceToModule = (preference) => {
        if (selectedModuleIndex === null) {
            alert('Please select a module first!');
            return;
        }

        setPreferenceModules((prevModules) => {
            if (!prevModules) return [];
            return prevModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    if (!module.preferences.some(p => p.identifier === preference.identifier)) {
                        return {
                            ...module,
                            preferences: [...module.preferences, preference],
                            involvedSets: [...new Set([...module.involvedSets, ...(preference.dep?.setDependencies || [])])].filter((set) => !bannedSets.includes(set)),
                            involvedParams: [...new Set([...module.involvedParams, ...(preference.dep?.paramDependencies || [])])].filter((param) => !bannedParams.includes(param))
                        };
                    }
                }
                return module;
            });
        });

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

        setPreferenceModules((prevModules) =>
            prevModules.map((module, idx) => {
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
        
        preferenceModules[index].preferences.forEach((preference) => removePreferenceFromModule(preference))
        setPreferenceModules((prevModules) => prevModules.filter((_, i) => i !== index));
    
        // Reset selection if the deleted module was selected
        if (selectedModuleIndex === index) {
            setSelectedModuleIndex(null);
        } else if (selectedModuleIndex > index) {
            setSelectedModuleIndex(selectedModuleIndex - 1); // Adjust index if needed
        }
    };

    const handleToggleInvolvedSet = (setName) => {
        setPreferenceModules((prevModules) =>
            prevModules.map((module, idx) => {
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
        setPreferenceModules((prevModules) =>
            prevModules.map((module, idx) => {
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
        <div className="configure-constraints-page">
            <h1 className="page-title">Configure Preference Modules</h1>

            <div className="constraints-layout">
                {/* Preference Modules Section */}
                <div className="constraint-modules">
                    <h2>Preference Modules</h2>
                    <input
                        type="text"
                        placeholder="Module Name"
                        value={moduleName}
                        onChange={(e) => setModuleName(e.target.value)}
                    />
                    <button onClick={addPreferenceModule}>Add Preference Module</button>
                    <div className="module-list">
                        {preferenceModules.map((module, index) => (
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

                {/* Define Preference Module Section */}
                <div className="define-constraint-module">
                    <h2>Define Preference Module</h2>
                    {selectedModuleIndex === null ? (
                        <p>Select a module</p>
                    ) : (
                        <>
                            <h3>{preferenceModules[selectedModuleIndex]?.name || 'Unnamed Module'}</h3>
                            <label>Description:</label>
                            <hr />
                            <textarea
                                value={preferenceModules[selectedModuleIndex]?.description || ""}
                                onChange={(e) => updateModuleDescription(e.target.value)}
                                placeholder="Enter module description..."
                                style={{ resize: "none", width: "100%", height: "80px" }}
                            />
                            <p>This module's preferences:</p>
                            <hr />
                            <div className="module-drop-area">
                                {preferenceModules[selectedModuleIndex]?.preferences?.length > 0 ? (
                                    preferenceModules[selectedModuleIndex].preferences.map((p, i) => (
                                        <div 
                                            key={i} 
                                            className="dropped-constraint constraint-box"
                                            onClick={() => removePreferenceFromModule(p)}
                                        >
                                            {p.identifier}
                                        </div>
                                    ))
                                ) : (
                                    <p>No preferences added</p>
                                )}
                            </div>
                            <h3>Select input Sets:</h3>
                            <div>
                                {preferenceModules[selectedModuleIndex]?.involvedSets.map((set, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={preferenceModules[selectedModuleIndex]?.inputSets.includes(set)} 
                                            onChange={() => handleToggleInvolvedSet(set)}
                                        /> {set}
                                    </div>
                                ))}
                            </div>

                            <h3>Select input Parameters:</h3>
                            <div>
                                {preferenceModules[selectedModuleIndex]?.involvedParams.map((param, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={preferenceModules[selectedModuleIndex]?.inputParams.includes(param)} 
                                            onChange={() => handleToggleInvolvedParam(param)}
                                        /> {param}
                                    </div>
                                ))}
                            </div>
                        </>
                    )}
                </div>

                {/* Available Preferences Section */}
                <div className="available-constraints">
                    <h2>Available Preferences</h2>
                    {availablePreferences.length > 0 ? (
                        availablePreferences.map((preference, idx) => (
                            <div key={idx} className="constraint-item-container">
                                <button className="constraint-item" onClick={() => addPreferenceToModule(preference)}>
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
                onClick={() => navigate('/solution-preview')}
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
