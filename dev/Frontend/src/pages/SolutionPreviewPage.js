import React from 'react';
import { Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './SolutionPreviewPage.css';

const SolutionPreviewPage = () => {
    // Retrieve constraint and preference modules from context
    const { modules, preferenceModules } = useZPL();

    return (
        <div className="solution-preview-page">
            <h1 className="page-title">Solution Preview</h1>

            <div className="modules-container">
                {/* Constraint Modules Section */}
                <div className="module-section">
                    <h2>Constraint Modules</h2>
                    {modules.length > 0 ? (
                        modules.map((module, index) => (
                            <div key={index} className="module-box">
                                <h3 className="module-title">{module.name}</h3>
                                <p className="module-subtitle">Constraints:</p>
                                <ul className="module-list">
                                    {module.constraints.map((constraint, i) => (
                                        <li key={i} className="module-item">{constraint.identifier}</li>
                                    ))}
                                </ul>
                            </div>
                        ))
                    ) : (
                        <p className="empty-message">No constraint modules defined.</p>
                    )}
                </div>

                {/* Preference Modules Section */}
                <div className="module-section">
                    <h2>Preference Modules</h2>
                    {preferenceModules.length > 0 ? (
                        preferenceModules.map((module, index) => (
                            <div key={index} className="module-box">
                                <h3 className="module-title">{module.name}</h3>
                                <p className="module-subtitle">Preferences:</p>
                                <ul className="module-list">
                                    {module.preferences.map((preference, i) => (
                                        <li key={i} className="module-item">{preference.identifier}</li>
                                    ))}
                                </ul>
                            </div>
                        ))
                    ) : (
                        <p className="empty-message">No preference modules defined.</p>
                    )}
                </div>
            </div>

            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default SolutionPreviewPage;
