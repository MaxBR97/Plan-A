import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigurePreferencesPage.css';
import '../styles/shared/ModuleConfig.css';
import Checkbox from '../reusableComponents/Checkbox';
import InfoIcon from '../reusableComponents/InfoIcon';

const ConfigurePreferencesPage = () => {
    const navigate = useNavigate();
    const {
        image,
        model,
        updateImageField
    } = useZPL();

    // Helper function to filter duplicate preferences
    const filterDuplicatePreferences = (preferences) => {
        return preferences.filter((pref, index, self) => 
            index === self.findIndex(p => p.identifier === pref.identifier)
        );
    };

    // Initialize state from existing imageDTO values
    const [availablePreferences, setAvailablePreferences] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [moduleDescription, setModuleDescription] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);
    
    // Get banned sets and params from variables module
    const bannedSets = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []))];
    const bannedParams = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []))];
    
    // Initialize modules from image DTO
    const [allModules, setAllModules] = useState(() => {
        const modules = Array.isArray(image.preferenceModules) ? image.preferenceModules : [];
        return modules.map(module => ({
            ...module,
            preferences: Array.isArray(module.preferences) ? module.preferences : [],
            inputSets: Array.isArray(module.inputSets) ? module.inputSets : [],
            inputParams: Array.isArray(module.inputParams) ? module.inputParams : [],
            costParams: Array.isArray(module.costParams) ? module.costParams : []
        }));
    });

    const [involvedSets, setInvolvedSets] = useState([]);
    const [involvedParams, setInvolvedParams] = useState([]);
    const [selectedSets, setSelectedSets] = useState([]);
    const [selectedParams, setSelectedParams] = useState([]);
    const [selectedPreference, setSelectedPreference] = useState(null);
    const [selectedCostParam, setSelectedCostParam] = useState(null);

    

    // Update allModules when image changes
    useEffect(() => {
        
        const modules = Array.isArray(image.preferenceModules) ? image.preferenceModules : [];
        const updatedModules = modules.map(module => ({
            ...module,
            preferences: Array.isArray(module.preferences) ? module.preferences : [],
            inputSets: Array.isArray(module.inputSets) ? module.inputSets : [],
            inputParams: Array.isArray(module.inputParams) ? module.inputParams : [],
            costParams: Array.isArray(module.costParams) ? module.costParams : []
        }));
        setAllModules(updatedModules);
    }, [image.preferenceModules]);

    useEffect(() => {
        // Initialize available preferences based on what's not already used in modules
        const usedPreferences = new Set(allModules.flatMap(m => m.preferences));
        const available = filterDuplicatePreferences(
            Array.from(model.preferences).filter(p => !usedPreferences.has(p.identifier))
        );
        setAvailablePreferences(available);
    }, [model.preferences, allModules]);

    useEffect(() => {
        if (selectedModuleIndex !== null && allModules.length > 0) {

            const module = allModules[selectedModuleIndex];
            setModuleName(module.moduleName);
            setModuleDescription(module.description);
            setSelectedPreference(module.preferences[0]);
            setSelectedCostParam(module.costParams?.[0]?.name);
            
            // Calculate involved sets and params
            const modulePreference = Array.from(model.preferences)
                .find(p => p.identifier === module.preferences[0]);

            if (modulePreference) {
                const sets = new Set(modulePreference.dep?.setDependencies || []);
                const params = new Set(modulePreference.dep?.paramDependencies || []);
                setInvolvedSets(Array.from(sets));
                setInvolvedParams(Array.from(params));
                setSelectedSets(module.inputSets?.map(s => s.name) || []);
                setSelectedParams(module.inputParams?.map(p => p.name) || []);
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
        updateImageField("preferenceModules", updatedModules);
        
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
        console.log("update occured")
        setAllModules(updatedModules);
        updateImageField("preferenceModules", updatedModules);
    };

    const handleDeleteModule = (index) => {
        const moduleToDelete = allModules[index];
        const preferenceToRestore = Array.from(model.preferences)
            .find(p => p.identifier === moduleToDelete.preferences[0]);

        if (preferenceToRestore) {
            setAvailablePreferences(prev => {
                const updated = [...prev];
                if (!prev.some(p => p.identifier === preferenceToRestore.identifier)) {
                    updated.push(preferenceToRestore);
                }
                return filterDuplicatePreferences(updated);
            });
        }

        const updatedModules = allModules.filter((_, i) => i !== index);
        setAllModules(updatedModules);
        updateImageField("preferenceModules", updatedModules);
        
        if (selectedModuleIndex === index) {
            setSelectedModuleIndex(null);
        } else if (selectedModuleIndex > index) {
            setSelectedModuleIndex(selectedModuleIndex - 1);
        }
    };

    const handleContinue = () => {
        // No need to update here anymore as changes are persisted immediately
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

        // Check if used in other modules, excluding the current module being edited
        const usedInOtherModules = allModules.some((module, index) => {
            // Skip the current module being edited
            if (index === selectedModuleIndex) return false;
            
            return isSet 
                ? module.inputSets.some(s => s.name === inputName)
                : module.inputParams.some(p => p.name === inputName);
        });
        if (usedInOtherModules) return false;

        return true;
    };

    const handlePreferenceSelect = (preference) => {
        if (selectedPreference === preference.identifier) {
            // Restore the unselected preference to available list in all cases
            const preferenceObj = Array.from(model.preferences)
                .find(p => p.identifier === preference.identifier);
            if (preferenceObj) {
                setAvailablePreferences(prev => {
                    const updated = [...prev];
                    if (!prev.some(p => p.identifier === preferenceObj.identifier)) {
                        updated.push(preferenceObj);
                    }
                    return filterDuplicatePreferences(updated);
                });
            }
            
            setSelectedPreference(null);
            setInvolvedSets([]);
            setInvolvedParams([]);
            setSelectedSets([]);
            setSelectedParams([]);
            setSelectedCostParam(null);
        } else {
            // If there's already a selected preference, restore it to the available list first
            if (selectedPreference) {
                const currentPreferenceObj = Array.from(model.preferences)
                    .find(p => p.identifier === selectedPreference);
                if (currentPreferenceObj) {
                    setAvailablePreferences(prev => {
                        const updated = [...prev];
                        if (!prev.some(p => p.identifier === currentPreferenceObj.identifier)) {
                            updated.push(currentPreferenceObj);
                        }
                        return filterDuplicatePreferences(updated);
                    });
                }
            }

            setSelectedPreference(preference.identifier);
            
            // When selecting a preference while editing a module, restore its previous inputs if they exist
            if (selectedModuleIndex !== null) {
                const currentModule = allModules[selectedModuleIndex];
                const wasPreferenceInModule = currentModule.preferences[0] === preference.identifier;
                
                if (wasPreferenceInModule) {
                    // Restore the module's previous input selections
                    setSelectedSets(currentModule.inputSets.map(s => s.name));
                    setSelectedParams(currentModule.inputParams.map(p => p.name));
                    setSelectedCostParam(currentModule.costParams?.[0]?.name);
                }
            }

            // Filter sets and params based on availability
            const availableSets = (preference.dep?.setDependencies || [])
                .filter(set => isInputAvailable(set, true));
            const availableParams = (preference.dep?.paramDependencies || [])
                .filter(param => isInputAvailable(param, false));
            
            setInvolvedSets(availableSets);
            setInvolvedParams(availableParams);
            
            // Only set selected inputs if we're not restoring a previous state
            if (!(selectedModuleIndex !== null && allModules[selectedModuleIndex].preferences[0] === preference.identifier)) {
                setSelectedSets(availableSets);
                setSelectedParams(availableParams);
                setSelectedCostParam(null);
            }
            
            // Remove the newly selected preference from available list and ensure no duplicates
            setAvailablePreferences(prev => 
                filterDuplicatePreferences(prev.filter(p => p.identifier !== preference.identifier))
            );
        }
    };

    // Update the preference display in the module configuration
    const renderPreferenceIdentifier = (identifier) => {
        if (!identifier) return null;
        if (identifier.length > 50) {
            return `${identifier.slice(0, 47)}...`;
        }
        return identifier;
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
                Create and manage optimization modules. Optimization modules provide a way to adjust the optimization objective, by balancing between trade-offs.
                Each Preference Module ideally represents a distinct optimization goal, which translates to a mathematical expression in the model.
                By setting a 'cost parameter' which represents the importance of the module's optimization goal, you can easily balance between the different optimization goals in the image.
            </p>
            
            <div className="preferences-layout">
                {/* Left Panel - Module List */}
                <div className="module-list-panel" onClick={handleModuleClick}>
                    <div className="panel-header">
                        <h2>
                            <span>Available Modules</span>
                            <InfoIcon tooltip="Here you can see all the modules you have created. You can edit or delete them as needed." />
                        </h2>
                    </div>
                    <div className="module-list" role="list" aria-label="preference modules" data-testid="module-list">
                        {allModules.map((module, index) => (
                            <div 
                                key={index} 
                                className={`module-item ${selectedModuleIndex === index ? 'selected' : ''}`}
                                onClick={() => setSelectedModuleIndex(index)}
                                role="listitem"
                            >
                                <div className="module-item-header">
                                    <span className="module-name">{module.moduleName}</span>
                                    <button 
                                        className="delete-button"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleDeleteModule(index);
                                        }}
                                        aria-label={`Delete ${module.moduleName}`}
                                    >
                                        ×
                                    </button>
                                </div>
                                <div className="module-item-details">
                                    <span className="preference-name">
                                        {module.preferences[0]}
                                    </span>
                                    <span className="cost-param">
                                        Cost: {module.costParams?.[0]?.name}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Middle Panel - Module Configuration */}
                <div className={`module-config-panel ${selectedModuleIndex !== null ? 'has-selection' : ''}`} onClick={handleModuleClick}>
                    <div className="panel-header">
                        <h2>Module Configuration</h2>
                    </div>
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
                            <label>Selected Optimization Expression</label>
                            <div className="selected-preference">
                                {selectedPreference ? (
                                    <div 
                                        className="preference-tag"
                                        onClick={() => handlePreferenceSelect({ identifier: selectedPreference })}
                                        title={selectedPreference}
                                    >
                                        {renderPreferenceIdentifier(selectedPreference)}
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
                        <h2>Available Expressions</h2>
                        <InfoIcon tooltip="List of the parsed expressions of your optimization function in the model you provided. Select a component which correlates to an optimization goal." />
                        {/* <div className="info-icon" title="Select from these available preferences to add to your module. Each preference can only be used in one module.">
                            ℹ️
                        </div> */}
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
                {/* <button className="back-button" onClick={handleBack}>
                    Back
                </button> */}
                <button className="continue-button" onClick={handleContinue}>
                    Continue
                </button>
            </div>
        </div>
    );
};

export default ConfigurePreferencesPage;
