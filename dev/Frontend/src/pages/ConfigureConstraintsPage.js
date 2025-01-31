import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDrag, useDrop, DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import './ConfigureConstraintsPage.css';

const ITEM_TYPE = 'CONSTRAINT';

// Constraint Item Component
const ConstraintItem = ({ constraint }) => {
    const [{ isDragging }, drag] = useDrag(() => ({
        type: ITEM_TYPE,
        item: { identifier: constraint.identifier },
        collect: (monitor) => ({
            isDragging: monitor.isDragging(),
        }),
    }));

    return (
        <div ref={drag} className="constraint-item" style={{ opacity: isDragging ? 0.5 : 1 }}>
            {constraint.identifier}
        </div>
    );
};

const ConfigureConstraintsPage = () => {
    const navigate = useNavigate();

    // Manage Modules & Available Constraints LOCALLY
    const [modules, setModules] = useState([]); // Modules are now local
    const [availableConstraints, setAvailableConstraints] = useState([
        { identifier: 'trivial1' },
        { identifier: 'trivial2' },
        { identifier: 'minGuardsCons' },
        { identifier: 'maxGuardsCons' },
    ]); 
    const [moduleName, setModuleName] = useState('');
    const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);

    const addConstraintModule = () => {
        if (moduleName.trim() !== '') {
            setModules((prevModules) => [...prevModules, { name: moduleName, constraints: [] }]);
            setModuleName('');
        }
    };

    // Drag & Drop Logic for Dropping into the Middle Section
    const [{ isOver }, drop] = useDrop(() => ({
        accept: ITEM_TYPE,
        drop: (item) => {
            if (selectedModuleIndex !== null) {
                setModules((prevModules) => {
                    return prevModules.map((module, index) => {
                        if (index === selectedModuleIndex) {
                            if (!module.constraints.some(c => c.identifier === item.identifier)) {
                                return { ...module, constraints: [...module.constraints, item] };
                            }
                        }
                        return module;
                    });
                });

                // Remove constraint from available list
                setAvailableConstraints((prevConstraints) =>
                    prevConstraints.filter((c) => c.identifier !== item.identifier)
                );
            }
        },
        collect: (monitor) => ({
            isOver: monitor.isOver(),
        }),
    }));

    return (
        <DndProvider backend={HTML5Backend}>
            <div className="configure-constraints-page">
                <h1 className="page-title">Configure High-Level Constraints</h1>
                
                <div className="constraints-layout">
                    {/* Constraint Modules Menu */}
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
                                    <button onClick={() => setSelectedModuleIndex(index)}>Select</button>
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* Define Constraint Module */}
                    <div className="define-constraint-module" ref={drop} style={{ backgroundColor: isOver ? '#e6ffe6' : '#f0f0f0' }}>
                        <h2>Define Constraint Module</h2>
                        {selectedModuleIndex === null || !modules[selectedModuleIndex] ? (
                            <p>Select a module</p>
                        ) : (
                            <>
                                <h3>{modules[selectedModuleIndex]?.name}</h3>
                                <p>Drag constraints here to define a module</p>
                                <div className="module-drop-area">
                                    {modules[selectedModuleIndex]?.constraints?.length > 0 ? (
                                        modules[selectedModuleIndex].constraints.map((c, i) => (
                                            <div key={i} className="dropped-constraint">{c.identifier}</div>
                                        ))
                                    ) : (
                                        <p>No constraints added</p>
                                    )}
                                </div>
                            </>
                        )}
                    </div>

                    {/* Available Constraints */}
                    <div className="available-constraints">
                        <h2>Available Constraints</h2>
                        {availableConstraints.length > 0 ? (
                            availableConstraints.map((constraint, index) => (
                                <ConstraintItem key={index} constraint={constraint} />
                            ))
                        ) : (
                            <p>No constraints available</p>
                        )}
                    </div>
                </div>
                
                <button className="continue-button" onClick={() => navigate('/configure-preferences')}>
                    Continue
                </button>
                
                <Link to="/" className="back-button">Back</Link>
            </div>
        </DndProvider>
    );
};

export default ConfigureConstraintsPage;
