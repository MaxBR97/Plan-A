import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';
import '../styles/shared/ModuleConfig.css';
import Checkbox from '../reusableComponents/Checkbox';
import InfoIcon from '../reusableComponents/InfoIcon';

const ConfigureConstraintsPage = () => {
    const navigate = useNavigate();
    const {
        image,
        model,
        updateImageField
    } = useZPL();

    // Get banned sets and params from variables module
    const bannedSets = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []))];
    const bannedParams = [...new Set(Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []))];

    // Initialize state from existing imageDTO values
    const [availableConstraints, setAvailableConstraints] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [moduleDescription, setModuleDescription] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);
    
    // Initialize modules from image DTO
    const [allModules, setAllModules] = useState(() => {
        return Array.from(image.constraintModules || []).map(module => ({
            ...module,
            constraints: Array.isArray(module.constraints) ? module.constraints : [],
            inputSets: Array.isArray(module.inputSets) ? module.inputSets : [],
            inputParams: Array.isArray(module.inputParams) ? module.inputParams : []
        }));
    });

    const [involvedSets, setInvolvedSets] = useState([]);
    const [involvedParams, setInvolvedParams] = useState([]);
    const [selectedSets, setSelectedSets] = useState([]);
    const [selectedParams, setSelectedParams] = useState([]);
    const [selectedConstraints, setSelectedConstraints] = useState([]);

    console.log("cur image:", image);
    console.log("bannedSets:", bannedSets);
    console.log("involvedSets:", involvedSets);

    console.log("allModules:", allModules);


    useEffect(() => {
        // Initialize available constraints based on what's not already used in modules
        const updatedAvailableConstraints = Array.from(model.constraints).filter((c) => 
            allModules.every((module) => 
                !module.constraints.some(constraint => 
                    typeof constraint === 'string' 
                        ? constraint === c.identifier 
                        : constraint.identifier === c.identifier
                )
            )
        );
        setAvailableConstraints(updatedAvailableConstraints);
    }, [model.constraints, allModules]);

    useEffect(() => {
        if (selectedModuleIndex !== null && allModules.length > 0) {
            const module = allModules[selectedModuleIndex];
            setModuleName(module.moduleName);
            setModuleDescription(module.description);
            setSelectedConstraints(module.constraints);
            
            // Calculate involved sets and params
            const moduleConstraints = module.constraints.map(c => 
                Array.from(model.constraints).find(mc => mc.identifier === c)
            ).filter(Boolean);

            const sets = new Set();
            const params = new Set();

            moduleConstraints.forEach(constraint => {
                (constraint.dep?.setDependencies || []).filter(set => !bannedSets.includes(set)).forEach(set => sets.add(set));
                (constraint.dep?.paramDependencies || []).filter(param => !bannedParams.includes(param)).forEach(param => params.add(param));
            });

            const involvedSetsArray = Array.from(sets);
            const involvedParamsArray = Array.from(params);

            setInvolvedSets(involvedSetsArray);
            setInvolvedParams(involvedParamsArray);
            
            // Set the selected inputs based on the module's configuration
            setSelectedSets(module.inputSets.map(s => s.name));
            setSelectedParams(module.inputParams.map(p => p.name));
        } else {
            setModuleName('');
            setModuleDescription('');
            setSelectedConstraints([]);
            setInvolvedSets([]);
            setInvolvedParams([]);
            setSelectedSets([]);
            setSelectedParams([]);
        }
    }, [selectedModuleIndex, allModules]);

    const handleCreateModule = () => {
        if (!moduleName.trim()) return;

        const newModule = {
            moduleName: moduleName.trim(),
            description: moduleDescription.trim(),
            constraints: selectedConstraints,
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
        updateImageField("constraintModules", updatedModules);
        
        setModuleName('');
        setModuleDescription('');
        setSelectedConstraints([]);
        setInvolvedSets([]);
        setInvolvedParams([]);
        setSelectedSets([]);
        setSelectedParams([]);
        setSelectedModuleIndex(null);
    };

    const handleUpdateModule = () => {
        if (selectedModuleIndex === null || !moduleName.trim()) return;

        const updatedModules = [...allModules];
        updatedModules[selectedModuleIndex] = {
            moduleName: moduleName.trim(),
            description: moduleDescription.trim(),
            constraints: selectedConstraints,
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
        updateImageField("constraintModules", updatedModules);
    };

    const handleDeleteModule = (index) => {
        const moduleToDelete = allModules[index];
        const constraintsToRestore = moduleToDelete.constraints.map(c => 
            Array.from(model.constraints).find(mc => mc.identifier === c)
        ).filter(Boolean);

        // setAvailableConstraints([...availableConstraints, ...constraintsToRestore]);
        const updatedModules = allModules.filter((_, i) => i !== index);
        setAllModules(updatedModules);
        updateImageField("constraintModules", updatedModules);
        
        if (selectedModuleIndex === index) {
            setSelectedModuleIndex(null);
        } else if (selectedModuleIndex > index) {
            setSelectedModuleIndex(selectedModuleIndex - 1);
        }
    };

    // Add this function to filter out already used sets and params
    const getAvailableSetsAndParams = (constraint) => {
        const usedSets = new Set([
            ...Array.from(model.variables).flatMap(v => v.dep?.setDependencies || []),
            ...allModules.flatMap(m => m.inputSets.map(s => s.name))
        ]);

        const usedParams = new Set([
            ...Array.from(model.variables).flatMap(v => v.dep?.paramDependencies || []),
            ...allModules.flatMap(m => m.inputParams.map(p => p.name))
        ]);

        const availableSets = (constraint.dep?.setDependencies || [])
            .filter(set => !usedSets.has(set));
        
        const availableParams = (constraint.dep?.paramDependencies || [])
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

    const handleConstraintSelect = (constraint) => {
        if (selectedConstraints.includes(constraint.identifier)) {
            // If clicking a selected constraint, remove it
            const updatedConstraints = selectedConstraints.filter(c => c !== constraint.identifier);
            setSelectedConstraints(updatedConstraints);
            
            // If we're editing a module, update available constraints
            if (selectedModuleIndex !== null) {
                setAvailableConstraints(prev => [...prev, constraint]);
            }
            
            // Recalculate involved sets and params
            const remainingConstraints = updatedConstraints
                .map(c => Array.from(model.constraints).find(mc => mc.identifier === c))
                .filter(Boolean);

            const sets = new Set();
            const params = new Set();

            remainingConstraints.forEach(constraint => {
                const availableSets = (constraint.dep?.setDependencies || [])
                    .filter(set => isInputAvailable(set, true) || selectedSets.includes(set));
                const availableParams = (constraint.dep?.paramDependencies || [])
                    .filter(param => isInputAvailable(param, false) || selectedParams.includes(param));
                
                availableSets.forEach(set => sets.add(set));
                availableParams.forEach(param => params.add(param));
            });

            setInvolvedSets(Array.from(sets));
            setInvolvedParams(Array.from(params));
            
            // Only reset selected inputs if they're no longer involved
            setSelectedSets(prev => prev.filter(set => sets.has(set)));
            setSelectedParams(prev => prev.filter(param => params.has(param)));
        } else {
            setSelectedConstraints([...selectedConstraints, constraint.identifier]);
            
            // If we're editing a module, update available constraints
            if (selectedModuleIndex !== null) {
                setAvailableConstraints(prev => prev.filter(c => c.identifier !== constraint.identifier));
            }
            
            // Add new sets and params
            const newSets = new Set(involvedSets);
            const newParams = new Set(involvedParams);

            const availableSets = (constraint.dep?.setDependencies || [])
                .filter(set => isInputAvailable(set, true) || selectedSets.includes(set));
            const availableParams = (constraint.dep?.paramDependencies || [])
                .filter(param => isInputAvailable(param, false) || selectedParams.includes(param));

            availableSets.forEach(set => newSets.add(set));
            availableParams.forEach(param => newParams.add(param));

            setInvolvedSets(Array.from(newSets));
            setInvolvedParams(Array.from(newParams));
            setSelectedSets(Array.from(newSets));
            setSelectedParams(Array.from(newParams));
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

    const handleContinue = () => {
        // No need to update here anymore as changes are persisted immediately
        navigate('/configuration-menu');
    };

    const handleBack = () => {
        // updateImageField("constraintModules", allModules);
        navigate('/configuration-menu');
    };

    const handleCancel = () => {
        // Only restore constraints to available list if we're creating a new module (not editing)
        if (selectedModuleIndex === null && selectedConstraints.length > 0) {
            const constraintsToRestore = selectedConstraints.map(constraintId => 
                Array.from(model.constraints).find(c => c.identifier === constraintId)
            ).filter(Boolean);
            
            setAvailableConstraints(prev => {
                const updated = [...prev];
                constraintsToRestore.forEach(constraint => {
                    if (!prev.some(c => c.identifier === constraint.identifier)) {
                        updated.push(constraint);
                    }
                });
                return updated;
            });
        }
        
        // Clear the form and selection
        setSelectedModuleIndex(null);
        setModuleName('');
        setModuleDescription('');
        setSelectedConstraints([]);
        setInvolvedSets([]);
        setInvolvedParams([]);
        setSelectedSets([]);
        setSelectedParams([]);
    };

    const handlePageClick = () => {
        // Do nothing when clicking outside - removed cancel functionality
    };

    const handleModuleClick = (e) => {
        // Prevent click from propagating to the page container
        e.stopPropagation();
    };

    return (
        <div className="configure-constraints-page" onClick={handlePageClick}>
            <h1 className="page-title">Configure Constraint Modules</h1>
            <p className="page-description">
            Create and manage constraint modules that can dynamically adjust the problem domain.
            Group related constraints together to form cohesive modules, each responsible for a specific aspect of the problem.
            Since you're familiar with your model, you can determine which constraints should be dynamically adjustable or toggled, and which should remain hardcoded in the model.
            </p>
            
            <div className="constraints-layout">
                {/* Left Panel - Module List */}
                <div className="module-list-panel" onClick={handleModuleClick}>
                    <div className="panel-header">
                        <h2>
                            <span>Available Modules</span>
                            <InfoIcon tooltip="Here you can see all the modules you have created. You can edit or delete them as needed." />
                        </h2>
                    </div>
                    <div className="module-list" role="list" aria-label="constraint modules">
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
                                    >
                                        ×
                                    </button>
                                </div>
                                <div className="module-item-details">
                                    <span className="constraint-count">
                                        {module.constraints.length} constraint{module.constraints.length !== 1 ? 's' : ''}
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
                            <label>Selected Constraints</label>
                            <div className="selected-constraints">
                                {selectedConstraints.map(constraintId => {
                                    const constraint = Array.from(model.constraints)
                                        .find(c => c.identifier === constraintId);
                                    return (
                                        <div 
                                            key={constraintId} 
                                            className="selected-constraint"
                                            onClick={() => handleConstraintSelect(constraint)}
                                        >
                                            {constraint?.identifier}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                        {selectedConstraints.length > 0 && (
                            <>
                                {involvedSets.length > 0 && <div className="form-group">
                                    <label>Input Sets</label>
                                    <div className="input-sets">
                                        {involvedSets.map(set => (
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
                                        {involvedParams.map(param => (
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
                            </>
                        )}
                        <button 
                            className="save-module-button"
                            onClick={selectedModuleIndex === null ? handleCreateModule : handleUpdateModule}
                            disabled={!moduleName.trim()}
                        >
                            {selectedModuleIndex === null ? 'Create Module' : 'Update Module'}
                        </button>
                        {(selectedModuleIndex !== null || selectedConstraints.length > 0) && (
                            <button 
                                className="cancel-button"
                                onClick={handleCancel}
                            >
                                Cancel
                            </button>
                        )}
                    </div>
                </div>

                {/* Right Panel - Available Constraints */}
                <div className="available-constraints-panel" onClick={handleModuleClick}>
                    <div className="panel-header">
                        <h2>Available Constraints</h2>
                        <InfoIcon tooltip="List of the parsed constraints from your model. Select a constraint to add it to your module." />
                    </div>
                    <div className="constraints-list">
                        {availableConstraints.map(constraint => {
                            const availableSets = (constraint.dep?.setDependencies || [])
                                .filter(set => isInputAvailable(set, true));
                            const availableParams = (constraint.dep?.paramDependencies || [])
                                .filter(param => isInputAvailable(param, false));
                            const hasAvailableInputs = availableSets.length > 0 || availableParams.length > 0;
                            
                            return (
                                <div 
                                    key={constraint.identifier}
                                    className={`constraint-item ${selectedConstraints.includes(constraint.identifier) ? 'selected' : ''}`}
                                    onClick={() => handleConstraintSelect(constraint)}
                                >
                                    {constraint.identifier}
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

export default ConfigureConstraintsPage;
