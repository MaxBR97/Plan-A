import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './SolutionPreviewPage.css';

const SolutionPreviewPage = () => {
    const { constraints, preferences, modules, preferenceModules, variables } = useZPL();
    const [variableValues, setVariableValues] = useState({});
    const [paramValues, setParamValues] = useState({});

    const handleAddValue = (setName) => {
        setVariableValues(prev => ({
            ...prev,
            [setName]: [...(prev[setName] || []), '']
        }));
    };

    const handleValueChange = (setName, index, value) => {
        setVariableValues(prev => {
            const newValues = [...prev[setName]];
            newValues[index] = value;
            return { ...prev, [setName]: newValues };
        });
    };

    const handleParamChange = (paramName, value) => {
        setParamValues(prev => ({
            ...prev,
            [paramName]: value
        }));
    };

    return (
        <div className="solution-preview-page">
            <h1 className="page-title">Solution Preview</h1>
            <div className="modules-container">
                
                {/* Constraints Section */}
                <div className="module-section">
                    <h2 className="section-title">Constraints</h2>
                    {modules.length > 0 ? (
                        modules.map((module, index) => (
                            <div key={index} className="module-box">
                                <h3 className="module-title">{module.name}</h3>
                                <p className="module-description"><strong>Module Description:</strong> {module.description}</p>
                                <h4 className="module-subtitle">Constraints</h4>
                                {module.constraints.length > 0 ? (
                                    module.constraints.map((constraint, cIndex) => (
                                        <div key={cIndex} className="module-item">
                                            <p><strong>Identifier:</strong> {constraint.identifier}</p>
                                        </div>
                                    ))
                                ) : (
                                    <p className="empty-message">No constraints in this module.</p>
                                )}
                                <h4 className="module-subtitle">Involved Sets</h4>
                                {module.involvedSets.length > 0 ? (
                                    module.involvedSets.map((set, sIndex) => (
                                        <div key={sIndex} className="module-item">{set}</div>
                                    ))
                                ) : (
                                    <p className="empty-message">No involved sets.</p>
                                )}
                                <h4 className="module-subtitle">Involved Parameters</h4>
                                {module.involvedParams.length > 0 ? (
                                    module.involvedParams.map((param, pIndex) => (
                                        <div key={pIndex} className="module-item">{param}</div>
                                    ))
                                ) : (
                                    <p className="empty-message">No involved parameters.</p>
                                )}
                            </div>
                        ))
                    ) : (
                        <p className="empty-message">No constraint modules available.</p>
                    )}
                </div>

                {/* Preferences Section */}
                <div className="module-section">
                    <h2 className="section-title">Preferences</h2>
                    {preferenceModules.length > 0 ? (
                        preferenceModules.map((module, index) => (
                            <div key={index} className="module-box">
                                <h3 className="module-title">{module.name}</h3>
                                <p className="module-description"><strong>Module Description:</strong> {module.description}</p>
                                <h4 className="module-subtitle">Preferences</h4>
                                {module.preferences.length > 0 ? (
                                    module.preferences.map((preference, pIndex) => (
                                        <div key={pIndex} className="module-item">
                                            <p><strong>Identifier:</strong> {preference.identifier}</p>
                                        </div>
                                    ))
                                ) : (
                                    <p className="empty-message">No preferences in this module.</p>
                                )}
                                <h4 className="module-subtitle">Involved Sets</h4>
                                {module.involvedSets.length > 0 ? (
                                    module.involvedSets.map((set, sIndex) => (
                                        <div key={sIndex} className="module-item">{set}</div>
                                    ))
                                ) : (
                                    <p className="empty-message">No involved sets.</p>
                                )}
                                <h4 className="module-subtitle">Involved Parameters</h4>
                                {module.involvedParams.length > 0 ? (
                                    module.involvedParams.map((param, pIndex) => (
                                        <div key={pIndex} className="module-item">{param}</div>
                                    ))
                                ) : (
                                    <p className="empty-message">No involved parameters.</p>
                                )}
                            </div>
                        ))
                    ) : (
                        <p className="empty-message">No preference modules available.</p>
                    )}
                </div>

                {/* Variable Sets Section */}
                <div className="module-section">
                    <h2 className="section-title">Variable Sets</h2>
                    {variables.map((variable, index) => (
                        variable.dep?.setDependencies?.map((set, sIndex) => (
                            <div key={`${index}-${sIndex}`} className="module-box">
                                <h3 className="module-title">{set}</h3>
                                <button className="add-button" onClick={() => handleAddValue(set)}></button>
                                {variableValues[set]?.map((value, vIndex) => (
                                    <input
                                        key={vIndex}
                                        type="text"
                                        value={value}
                                        onChange={(e) => handleValueChange(set, vIndex, e.target.value)}
                                        className="variable-input"
                                    />
                                ))}
                            </div>
                        ))
                    ))}
                </div>
            </div>
            
            <Link to="/configure-constraints" className="back-button">Back</Link>
        </div>
    );
};

export default SolutionPreviewPage;
