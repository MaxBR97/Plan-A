import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import { useDrag, useDrop, DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import './ConfigureConstraintsPage.css';

const ITEM_TYPE = 'CONSTRAINT';

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
  const { constraints: initialConstraints } = useZPL();
  const navigate = useNavigate();

  const [constraintModules, setConstraintModules] = useState([]);
  const [moduleName, setModuleName] = useState('');
  const [availableConstraints, setAvailableConstraints] = useState(initialConstraints);
  const [selectedModuleIndex, setSelectedModuleIndex] = useState(null);

  const addConstraintModule = () => {
    if (moduleName.trim() !== '') {
      setConstraintModules((prevModules) => [...prevModules, { name: moduleName, constraints: [] }]);
      setModuleName('');
    }
  };

  const [{ isOver }, drop] = useDrop(() => ({
    accept: ITEM_TYPE,
    drop: (item) => {
      if (selectedModuleIndex !== null) {
        setConstraintModules((prevModules) => {
          const newModules = prevModules.map((module, index) => {
            if (index === selectedModuleIndex) {
              return { ...module, constraints: [...module.constraints, item] };
            }
            return module;
          });
          return newModules;
        });

        // Remove constraint from available constraints
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
              {constraintModules.map((module, index) => (
                <li key={index}>
                  {module.name}
                  <button onClick={() => setSelectedModuleIndex(index)}>Select</button>
                </li>
              ))}
            </ul>
          </div>

          {/* Define Constraint Module */}
          <div className="define-constraint-module" ref={drop} style={{ backgroundColor: isOver ? '#f0f0f0' : 'white' }}>
            <h2>Define Constraint Module</h2>
            {selectedModuleIndex === null ? (
              <p>Select a module</p>
            ) : (
              <>
                <h3>{constraintModules[selectedModuleIndex].name}</h3>
                <p>Drag constraints here to define a module</p>
                <div className="module-drop-area">
                  {constraintModules[selectedModuleIndex].constraints.length > 0 ? (
                    constraintModules[selectedModuleIndex].constraints.map((c, i) => (
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
            {availableConstraints && availableConstraints.length > 0 ? (
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