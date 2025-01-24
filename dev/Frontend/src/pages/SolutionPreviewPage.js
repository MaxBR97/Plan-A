import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './SolutionPreviewPage.css';
import axios from 'axios';

const SolutionPreviewPage = () => {
    const { imageId, constraints, preferences, variables } = useZPL();
    const [status, setStatus] = useState('');

    const handleSolve = async () => {
        setStatus('Solving...');
        try {

            const requestData = {
                id: imageId,
                timeout: 10,
            };
            
            const response = await axios.post("/solve", requestData);

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'solution.json');
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);

            setStatus('');
        } catch (error) {
            setStatus(`Error: ${error.response?.data?.message || error.message}`);
            console.error('Error solving the problem:', error);
        }
    };

    return (
        <div className="solution-preview-page">
            <h1 className="page-title">Solution Preview</h1>

            {/* Constraints and Preferences Section */}
            <div className="constraints-preferences-section">
                <h2>Constraints</h2>
                {Array.isArray(constraints) && constraints.length > 0 ? (
                    constraints.map((constraint, index) => (
                        <div key={index} className="constraint-item">
                            <h3>{constraint.identifier}</h3>
                            <p>Set Dependencies: {Array.isArray(constraint.dep?.setDependencies) ? constraint.dep.setDependencies.join(', ') : 'None'}</p>
                            <p>Param Dependencies: {Array.isArray(constraint.dep?.paramDependencies) ? constraint.dep.paramDependencies.join(', ') : 'None'}</p>
                        </div>
                    ))
                ) : (
                    <p>No constraints available</p>
                )}

                <h2>Preferences</h2>
                {Array.isArray(preferences) && preferences.length > 0 ? (
                    preferences.map((preference, index) => (
                        <div key={index} className="preference-item">
                            <h3>{preference.identifier}</h3>
                            <p>Set Dependencies: {Array.isArray(preference.dep?.setDependencies) ? preference.dep.setDependencies.join(', ') : 'None'}</p>
                            <p>Param Dependencies: {Array.isArray(preference.dep?.paramDependencies) ? preference.dep.paramDependencies.join(', ') : 'None'}</p>
                        </div>
                    ))
                ) : (
                    <p>No preferences available</p>
                )}
            </div>

            {/* Variables Section */}
            <div className="variables-section">
                <h2>Variables</h2>
                {variables && typeof variables === 'object' && Object.entries(variables).length > 0 ? (
                    Object.entries(variables).map(([key, value]) => (
                        <div key={key} className="variable-item">
                            <h3>{key}</h3>
                            <ul>
                                {Array.isArray(value) ? (
                                    value.map((item, index) => <li key={index}>{item}</li>)
                                ) : (
                                    <p>No data available</p>
                                )}
                            </ul>
                            <button className="add-button">+</button>
                        </div>
                    ))
                ) : (
                    <p>No variables available</p>
                )}
            </div>

            {/* Solve Button and Status Label */}
            <button className="solve-button" onClick={handleSolve}>Solve</button>
            {status && <p className="status-label">{status}</p>}

            {/* Navigation Buttons */}
            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default SolutionPreviewPage;
