import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Import useNavigate
import './ConfigureConstraintsPage.css';

const ConfigureConstraintsPage = () => {
    const [constraints, setConstraints] = useState([
        { name: 'drisha 1', checked: true },
        { name: 'drisha 2', checked: true },
    ]);

    const navigate = useNavigate(); // Initialize navigate function

    // Handle checkbox change
    const handleCheckboxChange = (index) => {
        const updatedConstraints = [...constraints];
        updatedConstraints[index].checked = !updatedConstraints[index].checked;
        setConstraints(updatedConstraints);
    };

    // Handle adding a new constraint
    const handleAddConstraint = () => {
        setConstraints([...constraints, { name: `drisha ${constraints.length + 1}`, checked: false }]);
    };

    return (
        <div className="configure-constraints-page">
            <h1 className="page-title">Configure High-Level Constraints</h1>

            <div className="constraints-list">
                <h2>Constraint Names</h2>
                {constraints.map((constraint, index) => (
                    <div key={index} className="checkbox-item">
                        <input
                            type="checkbox"
                            checked={constraint.checked}
                            onChange={() => handleCheckboxChange(index)}
                        />
                        <label>{constraint.name}</label>
                    </div>
                ))}
            </div>

            <button className="add-button" onClick={handleAddConstraint}>
                Add Constraint
            </button>

            {/* "Continue" Button */}
            <button className="continue-button" onClick={() => navigate('/configure-preferences')}>
                Continue
            </button>

            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default ConfigureConstraintsPage;
