import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureConstraintsPage.css';

const ConfigureConstraintsPage = () => {
    const { constraints } = useZPL(); // Retrieve constraints from context
    const navigate = useNavigate();

    const [selectedConstraint, setSelectedConstraint] = useState(null);

    return (
        <div className="configure-constraints-page">
            <h1 className="page-title">Configure High-Level Constraints</h1>
            
            <div className="constraints-container">
                {/* Constraint Names Box */}
                <div className="constraints-list">
                    <h2>Constraint Names</h2>
                    {constraints && constraints.length > 0 ? (
                        constraints.map((constraint, index) => (
                            <div key={index} className="checkbox-item">
                                <input
                                    type="radio"
                                    name="constraint"
                                    onChange={() => setSelectedConstraint(constraint)}
                                />
                                <label>{constraint.identifier}</label>
                            </div>
                        ))
                    ) : (
                        <p>No constraints available</p>
                    )}
                </div>
                
                {/* Constraint Details Box */}
                <div className="constraint-details">
                    <h2>Constraint Details</h2>
                    {selectedConstraint ? (
                        <div>
                            <h3>Set Dependencies</h3>
                            {selectedConstraint.dep && selectedConstraint.dep.setDependencies && selectedConstraint.dep.setDependencies.length > 0 ? (
                                <ul>
                                    {selectedConstraint.dep.setDependencies.map((set, index) => (
                                        <li key={index}>{set}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No set dependencies</p>
                            )}
                            
                            <h3>Param Dependencies</h3>
                            {selectedConstraint.dep && selectedConstraint.dep.paramDependencies && selectedConstraint.dep.paramDependencies.length > 0 ? (
                                <ul>
                                    {selectedConstraint.dep.paramDependencies.map((param, index) => (
                                        <li key={index}>{param}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No param dependencies</p>
                            )}
                        </div>
                    ) : (
                        <p>Select a constraint to see details.</p>
                    )}
                </div>
            </div>
            
            <button className="continue-button" onClick={() => navigate('/configure-preferences')}>
                Continue
            </button>
            
            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default ConfigureConstraintsPage;
