import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';

const ConfigurePreferencesPage = () => {
    const navigate = useNavigate();

    // Fetch preferences & modules from ZPL context
    const { preferences: jsonPreferences = [], preferenceModules = [], setPreferenceModules = () => {} } = useZPL();

    // Local states
    const [availablePreferences, setAvailablePreferences] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);

    // Initialize available preferences dynamically from JSON
    useEffect(() => {
        setAvailablePreferences(jsonPreferences);
    }, [jsonPreferences]);

    // Add a new module
    const addPreferenceModule = () => {
        if (moduleName.trim() !== '') {
            setPreferenceModules((prevModules) => [
                ...prevModules,
                { name: moduleName, description: "", preferences: [], involvedSets: [], involvedParams: [] }
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
                            involvedSets: [...new Set([...module.involvedSets, ...(preference.dep?.setDependencies || [])])],
                            involvedParams: [...new Set([...module.involvedParams, ...(preference.dep?.paramDependencies || [])])]
                        };
                    }
                }
                return module;
            });
        });

        // Remove preference from the available list
        setAvailablePreferences((prev) =>
            prev.filter((p) => p.identifier !== preference.identifier)
        );
    };

    return (
        <div className="configure-constraints-page">
            <h1 className="page-title">Configure High-Level Preferences</h1>

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
                                        <div key={i} className="dropped-constraint">
                                            {p.identifier}
                                        </div>
                                    ))
                                ) : (
                                    <p>No preferences added</p>
                                )}
                            </div>
                            <h3>Involved Sets</h3>
                            <ul>
                                {preferenceModules[selectedModuleIndex]?.involvedSets.map((set, i) => (
                                    <li key={i}>{set}</li>
                                ))}
                            </ul>
                            <h3>Involved Parameters</h3>
                            <ul>
                                {preferenceModules[selectedModuleIndex]?.involvedParams.map((param, i) => (
                                    <li key={i}>{param}</li>
                                ))}
                            </ul>
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
