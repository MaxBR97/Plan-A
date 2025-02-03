import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';

const ConfigureConstraintsPage = () => {
    const navigate = useNavigate();

    // Fetch constraints & modules from ZPL context
    const { constraints: jsonConstraints = [], modules = [], setModules = () => {} } = useZPL();

    // Local states
    const [availableConstraints, setAvailableConstraints] = useState([]);
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);

    // Initialize available constraints dynamically from JSON
    useEffect(() => {
        setAvailableConstraints(jsonConstraints);
    }, [jsonConstraints]);

    // Add a new module
    const addConstraintModule = () => {
        if (moduleName.trim() !== '') {
            setModules((prevModules) => [
                ...prevModules,
                { name: moduleName, description: "", constraints: [], involvedSets: [], involvedParams: [] }
            ]);
            setModuleName('');
        }
    };

    // Update module description
    const updateModuleDescription = (newDescription) => {
        setModules((prevModules) =>
            prevModules.map((module, idx) =>
                idx === selectedModuleIndex ? { ...module, description: newDescription } : module
            )
        );
    };

    // Add constraint to selected module
    const addConstraintToModule = (constraint) => {
        if (selectedModuleIndex === null) {
            alert('Please select a module first!');
            return;
        }

        setModules((prevModules) => {
            if (!prevModules) return [];
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

        // Remove constraint from the available list
        setAvailableConstraints((prev) =>
            prev.filter((c) => c.identifier !== constraint.identifier)
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
                                        <div key={i} className="dropped-constraint">
                                            {c.identifier}
                                        </div>
                                    ))
                                ) : (
                                    <p>No constraints added</p>
                                )}
                            </div>
                            <h3>Involved Sets</h3>
                            <ul>
                                {modules[selectedModuleIndex]?.involvedSets.map((set, i) => (
                                    <li key={i}>{set}</li>
                                ))}
                            </ul>
                            <h3>Involved Parameters</h3>
                            <ul>
                                {modules[selectedModuleIndex]?.involvedParams.map((param, i) => (
                                    <li key={i}>{param}</li>
                                ))}
                            </ul>
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
