import React from 'react';
import { Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './SolutionPreviewPage.css';

const SolutionPreviewPage = () => {
    const { modules = [], preferenceModules = [] } = useZPL();

    return (
        <div className="solution-preview-page">
            <h1 className="page-title">Solution Preview</h1>

            <div className="modules-container">
                <div className="modules-section">
                    <h2>Constraint Modules</h2>
                    {modules.length > 0 ? (
                        modules.map((module, index) => (
                            <div key={index} className="module-box">
                                <h3>{module.name}</h3>
                                <p><strong>Description:</strong> {module.description || 'No description provided'}</p>
                                <hr />
                                <p><strong>Constraints:</strong></p>
                                {module.constraints.length > 0 ? (
                                    <ul>
                                        {module.constraints.map((constraint, i) => (
                                            <li key={i}>{constraint.identifier}</li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No constraints added</p>
                                )}
                                <hr />
                                <p><strong>Involved Sets:</strong></p>
                                {module.involvedSets.length > 0 ? (
                                    <ul>
                                        {module.involvedSets.map((set, i) => (
                                            <li key={i}>{set}</li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No involved sets</p>
                                )}
                                <hr />
                                <p><strong>Involved Parameters:</strong></p>
                                {module.involvedParams.length > 0 ? (
                                    <ul>
                                        {module.involvedParams.map((param, i) => (
                                            <li key={i}>{param}</li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No involved parameters</p>
                                )}
                            </div>
                        ))
                    ) : (
                        <p>No constraint modules defined.</p>
                    )}
                </div>

                <div className="modules-section">
                    <h2>Preference Modules</h2>
                    {preferenceModules.length > 0 ? (
                        preferenceModules.map((module, index) => (
                            <div key={index} className="module-box">
                                <h3>{module.name}</h3>
                                <p><strong>Description:</strong> {module.description || 'No description provided'}</p>
                                <hr />
                                <p><strong>Preferences:</strong></p>
                                {module.preferences.length > 0 ? (
                                    <ul>
                                        {module.preferences.map((preference, i) => (
                                            <li key={i}>{preference.identifier}</li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No preferences added</p>
                                )}
                                <hr />
                                <p><strong>Involved Sets:</strong></p>
                                {module.involvedSets.length > 0 ? (
                                    <ul>
                                        {module.involvedSets.map((set, i) => (
                                            <li key={i}>{set}</li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No involved sets</p>
                                )}
                                <hr />
                                <p><strong>Involved Parameters:</strong></p>
                                {module.involvedParams.length > 0 ? (
                                    <ul>
                                        {module.involvedParams.map((param, i) => (
                                            <li key={i}>{param}</li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No involved parameters</p>
                                )}
                            </div>
                        ))
                    ) : (
                        <p>No preference modules defined.</p>
                    )}
                </div>
            </div>

            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default SolutionPreviewPage;