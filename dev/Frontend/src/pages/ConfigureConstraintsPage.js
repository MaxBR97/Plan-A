import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';
import Checkbox from '../reusableComponents/Checkbox';

const ConfigureConstraintsPage = () => {
    const navigate = useNavigate();
    const { constraints: jsonConstraints = [], modules = [], setModules = () => {} } = useZPL();

    const [availableConstraints, setAvailableConstraints] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);

    useEffect(() => {
        setAvailableConstraints(jsonConstraints);
    }, [jsonConstraints]);

    const addConstraintModule = () => {
        if (moduleName.trim() !== '') {
            setModules((prevModules) => [
                ...prevModules,
                { name: moduleName, description: "", constraints: [], involvedSets: [], involvedParams: [] , inputSets:[], inputParams:[]}
            ]);
            setModuleName('');
        }
    };

    const updateModuleDescription = (newDescription) => {
        setModules((prevModules) =>
            prevModules.map((module, idx) =>
                idx === selectedModuleIndex ? { ...module, description: newDescription } : module
            )
        );
    };

    const addConstraintToModule = (constraint) => {
        if (selectedModuleIndex === null) {
            alert('Please select a module first!');
            return;
        }

        setModules((prevModules) => {
            return prevModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    if (!module.constraints.some(c => c.identifier === constraint.identifier)) {
                        return {
                            ...module,
                            constraints: [...module.constraints, constraint],
                            involvedSets: [...new Set([...module.involvedSets, ...(constraint.dep?.setDependencies || [])])],
                            involvedParams: [...new Set([...module.involvedParams, ...(constraint.dep?.paramDependencies || [])])]
                        };
                    }
                }
                return module;
            });
        });

        setAvailableConstraints((prev) =>
            prev.filter((c) => c.identifier !== constraint.identifier)
        );
    };

    const removeConstraintFromModule = (constraint) => {
        if (selectedModuleIndex === null) return;

        setModules((prevModules) =>
            prevModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    const newConstraints = module.constraints.filter(c => c.identifier !== constraint.identifier);

                    const remainingSets = new Set();
                    const remainingParams = new Set();
                    newConstraints.forEach(c => {
                        (c.dep?.setDependencies || []).forEach(set => remainingSets.add(set));
                        (c.dep?.paramDependencies || []).forEach(param => remainingParams.add(param));
                    });

                    return {
                        ...module,
                        constraints: newConstraints,
                        involvedSets: [...remainingSets],
                        involvedParams: [...remainingParams]
                    };
                }
                return module;
            })
        );

        setAvailableConstraints((prev) => [...prev, constraint]);
    };

    const deleteModule = (index) => {
        setModules((prevModules) => prevModules.filter((_, i) => i !== index));
    
        // Reset selection if the deleted module was selected
        if (selectedModuleIndex === index) {
            setSelectedModuleIndex(null);
        } else if (selectedModuleIndex > index) {
            setSelectedModuleIndex(selectedModuleIndex - 1); // Adjust index if needed
        }
    };

    //console.log(modules)
    const handleToggleInvolvedSet = (setName) => {
        setModules((prevModules) =>
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
        
        setModules((prevModules) =>
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
            <h1 className="page-title">Configure High-Level Constraints</h1>

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
                        {modules.map((module, index) => (
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

                {/* Define Constraint Module Section */}
                <div className="define-constraint-module">
                    <h2>Define Constraint Module</h2>
                    {selectedModuleIndex === null ? (
                        <p>Select a module</p>
                    ) : (
                        <>
                            <h3>{modules[selectedModuleIndex]?.name || 'Unnamed Module'}</h3>
                            <label>Description:</label>
                            <hr />
                            <textarea
                                value={modules[selectedModuleIndex]?.description || ""}
                                onChange={(e) => updateModuleDescription(e.target.value)}
                                placeholder="Enter module description..."
                                style={{ resize: "none", width: "100%", height: "80px" }}
                            />
                            <p>This module's constraints:</p>
                            <hr />
                            <div className="module-drop-area">
                                {modules[selectedModuleIndex]?.constraints?.length > 0 ? (
                                    modules[selectedModuleIndex].constraints.map((c, i) => (
                                        <div 
                                            key={i} 
                                            className="dropped-constraint constraint-box"
                                            onClick={() => removeConstraintFromModule(c)}
                                        >
                                            {c.identifier}
                                        </div>
                                    ))
                                ) : (
                                    <p>No constraints added</p>
                                )}
                            </div>

                            <h3>Select input Sets:</h3>
                            <div>
                                {modules[selectedModuleIndex]?.involvedSets.map((set, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={modules[selectedModuleIndex]?.inputSets.includes(set)} 
                                            onChange={() => handleToggleInvolvedSet(set)}
                                        /> {set}
                                    </div>
                                ))}
                            </div>

                            <h3>Select input Parameters:</h3>
                            <div>
                                {modules[selectedModuleIndex]?.involvedParams.map((param, i) => (
                                    <div key={i}>
                                        <Checkbox 
                                            type="checkbox" 
                                            checked={modules[selectedModuleIndex]?.inputParams.includes(param)} 
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
                onClick={() => navigate('/configure-preferences')}
            >
                Continue
            </button>

            <Link to="/" className="back-button">
                Back
            </Link>
        </div>
    );
};

export default ConfigureConstraintsPage;
