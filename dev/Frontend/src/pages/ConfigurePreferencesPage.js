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
            setPreferenceModules(prevModules => {
                if (!prevModules) return [];
                return [...prevModules, { name: moduleName, preferences: [] }];
            });
            setModuleName('');
        }
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
                    // Avoid duplicates
                    if (!module.preferences.some(p => p.identifier === preference.identifier)) {
                        return {
                            ...module,
                            preferences: [...module.preferences, preference]
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
                    <ul>
                        {preferenceModules.map((module, index) => (
                            <li key={index}>
                                {module.name}
                                <button onClick={() => setSelectedModuleIndex(index)}>
                                    Select
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>

                {/* Define Preference Module Section */}
                <div className="define-constraint-module">
                    <h2>Define Preference Module</h2>
                    {selectedModuleIndex === null ? (
                        <p>Select a module</p>
                    ) : (
                        <>
                            <h3>{preferenceModules[selectedModuleIndex]?.name || 'Unnamed Module'}</h3>
                            <p>This module's preferences:</p>
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
                        </>
                    )}
                </div>

                {/* Available Preferences Section */}
                <div className="available-constraints">
                    <h2>Available Preferences</h2>
                    {availablePreferences.length > 0 ? (
                        availablePreferences.map((preference, idx) => (
                            <div key={idx} className="constraint-item">
                                <span>{preference.identifier}</span>
                                <button onClick={() => addPreferenceToModule(preference)}>
                                    Add
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
