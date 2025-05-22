import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigurePreferencesPage.css';
import '../styles/shared/ModuleConfig.css';
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
    const [moduleDescription, setModuleDescription] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);
    const bannedSets = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []))];
    const bannedParams = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []))];
    const [allModules, setAllModules] = useState(Array.from(image.preferenceModules) || []);
    const [involvedSets, setInvolvedSets] = useState([]);
    const [involvedParams, setInvolvedParams] = useState([]);
    const [selectedSets, setSelectedSets] = useState([]);
    const [selectedParams, setSelectedParams] = useState([]);
    const [selectedPreference, setSelectedPreference] = useState(null);
    const [selectedCostParam, setSelectedCostParam] = useState(null);
    

    useEffect(() => {
        // Initialize available preferences
        setAvailablePreferences(Array.from(model.preferences).filter((p) => 
            allModules.every((module) => 
                module.preferences[0] !== p.identifier
            )
        ));
    }, [model.preferences, allModules]);

    useEffect(() => {
        if (selectedModuleIndex !== null && allModules.length > 0) {
            const module = allModules[selectedModuleIndex];
            setModuleName(module.moduleName);
            setModuleDescription(module.description);
            setSelectedPreference(module.preferences[0]);
            setSelectedCostParam(module.costParam);
            
            // Calculate involved sets and params
            const modulePreference = Array.from(model.preferences)
                .find(p => p.identifier === module.preferences[0]);

            if (modulePreference) {
                const sets = new Set(modulePreference.dep?.setDependencies || []);
                const params = new Set(modulePreference.dep?.paramDependencies || []);
                setInvolvedSets(Array.from(sets));
                setInvolvedParams(Array.from(params));
            }
        } else {
            setModuleName('');
            setModuleDescription('');
            setSelectedPreference(null);
            setSelectedCostParam(null);
            setInvolvedSets([]);
            setInvolvedParams([]);
            setSelectedSets([]);
            setSelectedParams([]);
        }
    }, [selectedModuleIndex, allModules]);

    const handleCreateModule = () => {
        if (!moduleName.trim() || !selectedPreference || !selectedCostParam) return;

        const newModule = {
            moduleName: moduleName.trim(),
            description: moduleDescription.trim(),
            preferences: [selectedPreference],
            costParams: [{
                name: selectedCostParam,
                tag: model.paramTypes?.[selectedCostParam],
                type: model.paramTypes?.[selectedCostParam],
                alias: selectedCostParam
            }],
            inputSets: selectedSets.map(setName => ({
                name: setName,
                tags: model.setTypes?.[setName] || [],
                type: model.setTypes?.[setName] || []
            })),
            inputParams: selectedParams.map(paramName => ({
                name: paramName,
                tag: model.paramTypes?.[paramName],
                type: model.paramTypes?.[paramName]
            }))
        };
        
        const updatedModules = [...allModules, newModule];
        setAllModules(updatedModules);
        
        // Update available preferences immediately
        setAvailablePreferences(prev => 
            prev.filter(p => p.identifier !== selectedPreference)
        );
        
        // Reset form
        setModuleName('');
        setModuleDescription('');
        setSelectedPreference(null);
        setSelectedCostParam(null);
        setInvolvedSets([]);
        setInvolvedParams([]);
        setSelectedSets([]);
        setSelectedParams([]);
        setSelectedModuleIndex(null);
    };

    const handleUpdateModule = () => {
        if (selectedModuleIndex === null || !moduleName.trim() || !selectedPreference || !selectedCostParam) return;

        const updatedModules = [...allModules];
        updatedModules[selectedModuleIndex] = {
            moduleName: moduleName.trim(),
            description: moduleDescription.trim(),
            preferences: [selectedPreference],
            costParams: [{
                name: selectedCostParam,
                tag: model.paramTypes?.[selectedCostParam],
                type: model.paramTypes?.[selectedCostParam],
                alias: selectedCostParam
            }],
            inputSets: selectedSets.map(setName => ({
                name: setName,
                tags: model.setTypes?.[setName] || [],
                type: model.setTypes?.[setName] || []
            })),
            inputParams: selectedParams.map(paramName => ({
                name: paramName,
                tag: model.paramTypes?.[paramName],
                type: model.paramTypes?.[paramName]
            }))
        };

        setAllModules(updatedModules);
    };

    const handleDeleteModule = (index) => {
        const moduleToDelete = allModules[index];
        const preferenceToRestore = Array.from(model.preferences)
            .find(p => p.identifier === moduleToDelete.preferences[0]);

        if (preferenceToRestore) {
            setAvailablePreferences([...availablePreferences, preferenceToRestore]);
        }

        setAllModules(allModules.filter((_, i) => i !== index));
        
        if (selectedModuleIndex === index) {
            setSelectedModuleIndex(null);
        } else if (selectedModuleIndex > index) {
            setSelectedModuleIndex(selectedModuleIndex - 1);
        }
    };

    const handleContinue = () => {
        updateImageField("preferenceModules", allModules);
        navigate('/configuration-menu');
    };

    const handleBack = () => {
        updateImageField("preferenceModules", allModules);
        navigate('/configuration-menu');
    };

    const handleConfigSetsAndParams = () => {
        updateImageField("preferenceModules", allModules);
        navigate('/configure-sets-params');
    };

    const handleConfigSolver = () => {
        updateImageField("preferenceModules", allModules);
        navigate('/configure-solver-options');
    };

    // Add this function to filter out already used sets and params
    const getAvailableSetsAndParams = (preference) => {
        const usedSets = new Set([
            ...Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []),
            ...allModules.flatMap(m => m.inputSets.map(s => s.name))
        ]);

        const usedParams = new Set([
            ...Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []),
            ...allModules.flatMap(m => m.inputParams.map(p => p.name))
        ]);

        const availableSets = (preference.dep?.setDependencies || [])
            .filter(set => !usedSets.has(set));
        
        const availableParams = (preference.dep?.paramDependencies || [])
            .filter(param => !usedParams.has(param));

        return { availableSets, availableParams };
    };

    // Add this function to check if an input is available
    const isInputAvailable = (inputName, isSet = true) => {
        // Check if used in variables configuration
        const usedInVariables = Array.from(model.variables).some(v => 
            isSet 
                ? v.dep?.setDependencies?.includes(inputName)
                : v.dep?.paramDependencies?.includes(inputName)
        );
        if (usedInVariables) return false;

        // Check if used in other modules
        const usedInOtherModules = allModules.some(module => 
            isSet 
                ? module.inputSets.some(s => s.name === inputName)
                : module.inputParams.some(p => p.name === inputName)
        );
        if (usedInOtherModules) return false;

        return true;
    };

    const handlePreferenceSelect = (preference) => {
        if (selectedPreference === preference.identifier) {
            setSelectedPreference(null);
            setInvolvedSets([]);
            setInvolvedParams([]);
            setSelectedSets([]);
            setSelectedParams([]);
            setSelectedCostParam(null);
        } else {
            setSelectedPreference(preference.identifier);
            
            // Filter sets and params based on availability
            const availableSets = (preference.dep?.setDependencies || [])
                .filter(set => isInputAvailable(set, true));
            const availableParams = (preference.dep?.paramDependencies || [])
                .filter(param => isInputAvailable(param, false));
            
            setInvolvedSets(availableSets);
            setInvolvedParams(availableParams);
            setSelectedSets(availableSets);
            setSelectedParams(availableParams);
            setSelectedCostParam(null);
        }
    };

    const handleSetToggle = (setName) => {
        setSelectedSets(prev => 
            prev.includes(setName) 
                ? prev.filter(s => s !== setName)
                : [...prev, setName]
        );
    };

    const handleParamToggle = (paramName) => {
        setSelectedParams(prev => 
            prev.includes(paramName) 
                ? prev.filter(p => p !== paramName)
                : [...prev, paramName]
        );
    };
    

    const handleCostParamSelect = (paramName) => {
        setSelectedCostParam(paramName);
    };

    const handlePageClick = () => {
        // Clear the form and selection when clicking outside
        setSelectedModuleIndex(null);
        setModuleName('');
        setModuleDescription('');
        setSelectedPreference(null);
        setSelectedCostParam(null);
        setInvolvedSets([]);
        setInvolvedParams([]);
        setSelectedSets([]);
        setSelectedParams([]);
    };

    const handleModuleClick = (e) => {
        // Prevent click from propagating to the page container
        e.stopPropagation();
    };

    return (
        <div className="configure-preferences-page" onClick={handlePageClick}>
            <h1 className="page-title">Configure Preference Modules</h1>
            <p className="page-description">
                Create and manage preference modules to define optimization objectives. Each module represents a distinct optimization goal with its associated parameters and dependencies.
            </p>
            
            <div className="preferences-layout">
                {/* Left Panel - Module List */}
                <div className="module-list-panel" onClick={handleModuleClick}>
                    <div className="panel-header">
                        <h2>Preference Modules</h2>
                        <div className="info-icon" title="Preference modules group related optimization objectives. Each module can contain one preference with its associated parameters.">
                            ℹ️
                        </div>
                    </div>
                    <div className="module-list">
                        {allModules.map((module, index) => (
                            <div 
                                key={index} 
                                className={`module-item ${selectedModuleIndex === index ? 'selected' : ''}`}
                                onClick={() => setSelectedModuleIndex(index)}
                            >
                                <div className="module-item-header">
                                    <span className="module-name">{module.moduleName}</span>
                                    <button 
                                        className="delete-button"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleDeleteModule(index);
                                        }}
                                    >
                                        ×
                                    </button>
                                </div>
                                <div className="module-item-details">
                                    <span className="preference-name">
                                        {module.preferences[0]}
                                    </span>
                                    <span className="cost-param">
                                        Cost: {module.costParam}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Middle Panel - Module Configuration */}
                <div className={`module-config-panel ${selectedModuleIndex !== null ? 'has-selection' : ''}`} onClick={handleModuleClick}>
                    <h2>{selectedModuleIndex === null ? 'Create New Module' : 'Edit Module'}</h2>
                    <div className="module-form">
                        <div className="form-group">
                            <label>Module Name</label>
                            <input
                                type="text"
                                value={moduleName}
                                onChange={(e) => setModuleName(e.target.value)}
                                placeholder="Enter module name"
                            />
                        </div>
                        <div className="form-group">
                            <label>Description</label>
                            <textarea
                                value={moduleDescription}
                                onChange={(e) => setModuleDescription(e.target.value)}
                                placeholder="Enter module description"
                                rows="3"
                            />
                        </div>
                        <div className="form-group">
                            <label>Selected Preference</label>
                            <div className="selected-preference">
                                {selectedPreference ? (
                                    <div 
                                        className="preference-tag"
                                        onClick={() => handlePreferenceSelect({ identifier: selectedPreference })}
                                    >
                                        {selectedPreference}
                                    </div>
                                ) : (
                                    <div className="no-preference">No preference selected</div>
                                )}
                            </div>
                        </div>
                        {selectedPreference && (
                            <>
                                {involvedSets.length > 0 && <div className="form-group">
                                    <label>Input Sets</label>
                                    <div className="input-sets">
                                        {involvedSets.filter(set => !bannedSets.includes(set)).map(set => (
                                            <div key={set} className="input-item">
                                                <Checkbox
                                                    checked={selectedSets.includes(set)}
                                                    onChange={() => handleSetToggle(set)}
                                                    label={set}
                                                />
                                            </div>
                                        ))}
                                    </div>
                                </div>}
                                {involvedParams.length > 0 && <div className="form-group">
                                    <label>Input Parameters</label>
                                    <div className="input-params">
                                        {involvedParams.filter(param => !bannedParams.includes(param)).map(param => (
                                            <div key={param} className="input-item">
                                                <Checkbox
                                                    checked={selectedParams.includes(param)}
                                                    onChange={() => handleParamToggle(param)}
                                                    label={param}
                                                />
                                            </div>
                                        ))}
                                    </div>
                                </div>}
                                <div className="form-group">
                                    <label>Cost Parameter</label>
                                    <select
                                        value={selectedCostParam || ''}
                                        onChange={(e) => handleCostParamSelect(e.target.value)}
                                        className="cost-param-select"
                                    >
                                        <option value="">Select a cost parameter</option>
                                        {involvedParams
                                            .filter(param => {
                                                const paramType = model.paramTypes?.[param];
                                                return paramType === 'INT' || paramType === 'FLOAT';
                                            })
                                            .map(param => (
                                                <option key={param} value={param}>
                                                    {param}
                                                </option>
                                            ))
                                        }
                                    </select>
                                </div>
                            </>
                        )}
                        <button 
                            className="save-module-button"
                            onClick={selectedModuleIndex === null ? handleCreateModule : handleUpdateModule}
                            disabled={!moduleName.trim() || !selectedPreference || !selectedCostParam}
                        >
                            {selectedModuleIndex === null ? 'Create Module' : 'Update Module'}
                        </button>
                    </div>
                </div>

                {/* Right Panel - Available Preferences */}
                <div className="available-preferences-panel" onClick={handleModuleClick}>
                    <div className="panel-header">
                        <h2>Available Preferences</h2>
                        <div className="info-icon" title="Select from these available preferences to add to your module. Each preference can only be used in one module.">
                            ℹ️
                        </div>
                    </div>
                    <div className="preferences-list">
                        {availablePreferences.map(preference => {
                            const availableSets = (preference.dep?.setDependencies || [])
                                .filter(set => isInputAvailable(set, true));
                            const availableParams = (preference.dep?.paramDependencies || [])
                                .filter(param => isInputAvailable(param, false));
                            const hasAvailableInputs = availableSets.length > 0 || availableParams.length > 0;
                            
                            return (
                                <div 
                                    key={preference.identifier}
                                    className={`preference-item ${selectedPreference === preference.identifier ? 'selected' : ''}`}
                                    onClick={() => handlePreferenceSelect(preference)}
                                >
                                    {preference.identifier.length > 100 ? preference.identifier.slice(0, 100) + '...' : preference.identifier   }
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>

            <div className="navigation-buttons" onClick={handleModuleClick}>
                <button className="back-button" onClick={handleBack}>
                    Back
                </button>
                <button className="continue-button" onClick={handleContinue}>
                    Continue
                </button>
                <button className="config-button" onClick={handleConfigSetsAndParams}>
                    Configure Sets & Params
                </button>
                <button className="config-button" onClick={handleConfigSolver}>
                    Configure Solver
                </button>
            </div>
        </div>
    );
};

export default ConfigurePreferencesPage;
