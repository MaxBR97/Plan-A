import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './ConfigurePreferencesPage.css';

const ConfigurePreferencesPage = () => {
    // State for preferences
    const [preferences, setPreferences] = useState([
        { name: 'My Preference 1', checked: true },
        { name: 'My Preference 2', checked: true },
    ]);

    const [preferenceDescription, setPreferenceDescription] = useState('');
    const [adjustableBar, setAdjustableBar] = useState(50); // For the adjustable bar

    const navigate = useNavigate(); // Initialize the navigation function

    // Handle checkbox change
    const handleCheckboxChange = (index) => {
        const updatedPreferences = [...preferences];
        updatedPreferences[index].checked = !updatedPreferences[index].checked;
        setPreferences(updatedPreferences);
    };

    // Handle adding a new preference
    const handleAddPreference = () => {
        setPreferences([...preferences, { name: `My Preference ${preferences.length + 1}`, checked: false }]);
    };

    return (
        <div className="configure-preferences-page">
            <h1 className="page-title">Configure High-Level Preferences</h1>

            <div className="preferences-list">
                <h2>Preference Names</h2>
                {preferences.map((preference, index) => (
                    <div key={index} className="checkbox-item">
                        <input
                            type="checkbox"
                            checked={preference.checked}
                            onChange={() => handleCheckboxChange(index)}
                        />
                        <label>{preference.name}</label>
                    </div>
                ))}
            </div>

            <div className="preference-details">
                <h2>Preference Description</h2>
                <textarea
                    value={preferenceDescription}
                    onChange={(e) => setPreferenceDescription(e.target.value)}
                    placeholder="Enter preference description here..."
                />
            </div>

            <h2>Adjustable Bar</h2>
            <input
                type="range"
                min="0"
                max="100"
                value={adjustableBar}
                onChange={(e) => setAdjustableBar(e.target.value)}
            />
            <p>Value: {adjustableBar}</p>

            <button className="add-button" onClick={handleAddPreference}>
                Add Preference
            </button>

            {/* "Continue" Button to Navigate to Solution Preview Page */}
            <button className="continue-button" onClick={() => navigate('/solution-preview')}>
                Continue
            </button>

            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default ConfigurePreferencesPage;
