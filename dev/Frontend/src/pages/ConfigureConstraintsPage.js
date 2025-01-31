import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';

const ConfigureConstraintsPage = () => {
    const navigate = useNavigate();

    // Fetch constraints & modules from ZPL context
    const { constraints: jsonConstraints, modules, setModules } = useZPL();


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
                { name: moduleName, constraints: [] }
            ]);
            setModuleName('');
        }
    };

    // Add constraint to selected module
    const addConstraintToModule = (constraint) => {
        if (selectedModuleIndex === null) {
            alert('Please select a module first!');
            return;
        }

        setModules((prevModules) => {
            return prevModules.map((module, idx) => {
                if (idx === selectedModuleIndex) {
                    // Avoid duplicates
                    if (!module.constraints.some(c => c.identifier === constraint.identifier)) {
                        return {
                            ...module,
                            constraints: [...module.constraints, constraint]
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
                    <ul>
                        {modules.map((module, index) => (
                            <li key={index}>
                                {module.name}
                                <button onClick={() => setSelectedModuleIndex(index)}>
                                    Select
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>

                {/* Define Constraint Module Section */}
                <div className="define-constraint-module">
                    <h2>Define Constraint Module</h2>
                    {selectedModuleIndex === null ? (
                        <p>Select a module</p>
                    ) : (
                        <>
                            <h3>{modules[selectedModuleIndex].name}</h3>
                            <p>This module's constraints:</p>
                            <div className="module-drop-area">
                                {modules[selectedModuleIndex].constraints.length > 0 ? (
                                    modules[selectedModuleIndex].constraints.map((c, i) => (
                                        <div key={i} className="dropped-constraint">
                                            {c.identifier}
                                        </div>
                                    ))
                                ) : (
                                    <p>No constraints added</p>
                                )}
                            </div>
                        </>
                    )}
                </div>

                {/* Available Constraints Section */}
                <div className="available-constraints">
                    <h2>Available Constraints</h2>
                    {availableConstraints.length > 0 ? (
                        availableConstraints.map((constraint, idx) => (
                            <div key={idx} className="constraint-item">
                                <span>{constraint.identifier}</span>
                                <button onClick={() => addConstraintToModule(constraint)}>
                                    Add
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
