import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import { useDrag, useDrop, DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import './ConfigureConstraintsPage.css';

const ITEM_TYPE = 'CONSTRAINT';

const ConstraintItem = ({ constraint, onDrop }) => {
    const [{ isDragging }, drag] = useDrag(() => ({
        type: ITEM_TYPE,
        item: { constraint },
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
    const { constraints } = useZPL(); // Retrieve constraints from context
    const navigate = useNavigate();

    const [constraintModules, setConstraintModules] = useState([]);
    const [moduleName, setModuleName] = useState('');

    const addConstraintModule = () => {
        if (moduleName.trim() !== '') {
            setConstraintModules([...constraintModules, { name: moduleName, constraints: [] }]);
            setModuleName('');
        }
    };

    const [{ isOver }, drop] = useDrop(() => ({
        accept: ITEM_TYPE,
        drop: (item) => {
            setConstraintModules((prevModules) => {
                if (prevModules.length > 0) {
                    const newModules = [...prevModules];
                    newModules[newModules.length - 1].constraints.push(item.constraint);
                    return newModules;
                }
                return prevModules;
            });
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
                            {constraintModules.map((module, index) => (
                                <li key={index}>{module.name}</li>
                            ))}
                        </ul>
                    </div>

                    {/* Define Constraint Module */}
                    <div className="define-constraint-module" ref={drop} style={{ backgroundColor: isOver ? '#f0f0f0' : 'white' }}>
                        <h2>Define Constraint Module</h2>
                        <p>Drag constraints here to define a module</p>
                        <div className="module-drop-area">
                            {constraintModules.length > 0 && constraintModules[constraintModules.length - 1].constraints.length > 0 ? (
                                constraintModules[constraintModules.length - 1].constraints.map((c, i) => (
                                    <div key={i} className="dropped-constraint">{c.identifier}</div>
                                ))
                            ) : (
                                <p>No constraints added</p>
                            )}
                        </div>
                    </div>

                    {/* Available Constraints */}
                    <div className="available-constraints">
                        <h2>Available Constraints</h2>
                        {constraints && constraints.length > 0 ? (
                            constraints.map((constraint, index) => (
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
