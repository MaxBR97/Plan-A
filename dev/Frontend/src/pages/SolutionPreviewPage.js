import React from 'react';
import { Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './SolutionPreviewPage.css';

const SolutionPreviewPage = () => {
    const { constraints, preferences, modules, preferenceModules, variables } = useZPL();

    console.log("Modules:", modules);
    console.log("Preference Modules:", preferenceModules);
    console.log("Constraints:", constraints);
    console.log("Preferences:", preferences);
    console.log("Variables:", variables);

    const allSets = variables.flatMap(variable => variable.dep?.setDependencies ?? []);
    const allParams = variables.flatMap(variable => variable.dep?.paramDependencies ?? []);

    return (
        <div className="solution-preview-page">
            <h1 className="page-title">Solution Preview</h1>
            <div className="modules-grid-container">
                {/* Constraints Section */}
                <div className="module-section">
    <h2 className="section-title">Constraints</h2>
    {modules.length > 0 ? (
        <div className="module-box-container"> {/* Added wrapper to align modules */}
            {modules.map((module, index) => (
                <div key={index} className="module-box">
                    <h3 className="module-title">{module.name}</h3>
                    <p className="module-label"><strong>Module Description:</strong></p>
                    <p className="module-description">{module.description || "No description provided."}</p>

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
            ))}
        </div>
    ) : (
        <p className="empty-message">No constraint modules available.</p>
    )}
</div>

{/* Preferences Section */}
<div className="module-section">
    <h2 className="section-title">Preferences</h2>
    {preferenceModules.length > 0 ? (
        <div className="module-box-container"> {/* Added wrapper to align modules */}
            {preferenceModules.map((module, index) => (
                <div key={index} className="module-box">
                    <h3 className="module-title">{module.name}</h3>
                    <p className="module-label"><strong>Module Description:</strong></p>
                    <p className="module-description">{module.description || "No description provided."}</p>

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
            ))}
        </div>
    ) : (
        <p className="empty-message">No preference modules available.</p>
    )}
</div>

            </div>

            <div className="modules-container">
                {/* Variable Sets Section */}
                <div className="module-section">
                    <h2 className="section-title">Variable Sets</h2>
                    {allSets.length > 0 ? (
                        allSets.map((set, index) => (
                            <div key={index} className="module-item">
                                {set}
                            </div>
                        ))
                    ) : (
                        <p className="empty-message">No variable sets available.</p>
                    )}
                </div>

                {/* Variable Params Section */}
                <div className="module-section">
                    <h2 className="section-title">Variable Params</h2>
                    {allParams.length > 0 ? (
                        allParams.map((param, index) => (
                            <div key={index} className="module-item">
                                {param}
                            </div>
                        ))
                    ) : (
                        <p className="empty-message">No variable parameters available.</p>
                    )}
                </div>
            </div>
            
            <Link to="/configure-constraints" className="back-button">Back</Link>
        </div>
    );
};

export default SolutionPreviewPage;
