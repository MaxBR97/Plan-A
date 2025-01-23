import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigurePreferencesPage.css';

const ConfigurePreferencesPage = () => {
    const { preferences } = useZPL(); // Retrieve preferences from context
    const navigate = useNavigate();

    const [selectedPreference, setSelectedPreference] = useState(null);

    useEffect(() => {
        console.log("Stored Preferences Data:", preferences);
    }, [preferences]);

    return (
        <div className="configure-preferences-page">
            <h1 className="page-title">Configure High-Level Preferences</h1>

            <div className="preferences-container">
                {/* Preference Names Box */}
                <div className="preferences-list">
                    <h2>Preference Names</h2>
                    {preferences && preferences.length > 0 ? (
                        preferences.map((preference, index) => (
                            <div key={index} className="checkbox-item">
                                <input
                                    type="radio"
                                    name="preference"
                                    onChange={() => setSelectedPreference(preference)}
                                />
                                <label>{preference.identifier}</label>
                            </div>
                        ))
                    ) : (
                        <p>No preferences available</p>
                    )}
                </div>
                
                {/* Preference Details Box */}
                <div className="preference-details">
                    <h2>Preference Details</h2>
                    {selectedPreference ? (
                        <div>
                            <h3>Set Dependencies</h3>
                            {selectedPreference.dep && selectedPreference.dep.setDependencies && selectedPreference.dep.setDependencies.length > 0 ? (
                                <ul>
                                    {selectedPreference.dep.setDependencies.map((set, index) => (
                                        <li key={index}>{set}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No set dependencies</p>
                            )}
                            
                            <h3>Param Dependencies</h3>
                            {selectedPreference.dep && selectedPreference.dep.paramDependencies && selectedPreference.dep.paramDependencies.length > 0 ? (
                                <ul>
                                    {selectedPreference.dep.paramDependencies.map((param, index) => (
                                        <li key={index}>{param}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No param dependencies</p>
                            )}
                        </div>
                    ) : (
                        <p>Select a preference to see details.</p>
                    )}
                </div>
            </div>

            {/* "Continue" Button to Navigate to Solution Preview Page */}
            <button className="continue-button" onClick={() => navigate('/solution-preview')}>
                Continue
            </button>

            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default ConfigurePreferencesPage;
