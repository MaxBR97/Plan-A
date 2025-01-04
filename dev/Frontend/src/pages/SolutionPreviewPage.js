import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './SolutionPreviewPage.css';

const SolutionPreviewPage = () => {
    // State for constraints and preferences
    const [constraints, setConstraints] = useState([
        { name: 'My Constraint 1', sets: ['set 2'], params: ['param 1'], description: 'Constraint Description 1' },
        { name: 'My Constraint 2', sets: ['set 4'], params: ['param 2'], description: 'Constraint Description 2' },
    ]);

    const [preferences, setPreferences] = useState([
        { name: 'My Preference 1', sets: ['set 3'], params: ['param 1'], description: 'Preference Description 1' },
        { name: 'My Preference 2', sets: ['set 5'], params: ['param 3'], description: 'Preference Description 2' },
    ]);

    // State for variables
    const [people, setPeople] = useState(['Dani', 'Yossi', 'Meni', 'Aharon']);
    const [dates, setDates] = useState(['21/12/2024', '22/12/2024', '23/12/2024']);
    const [stations, setStations] = useState(['Station 1', 'Station 2', 'Station 3']);
    const [baseSalaryRate, setBaseSalaryRate] = useState(30.0);

    // Sample salary data
    const salaries = [
        { person: 'Dani', salary: 30 },
        { person: 'Yossi', salary: 30 },
        { person: 'Aharon', salary: 40 },
    ];

    return (
        <div className="solution-preview-page">
            <h1 className="page-title">Preview of Created Image</h1>

            <div className="main-section">
                {/* Left Section: Constraints and Preferences */}
                <div className="left-section">
                    <div className="constraints-section">
                        <h2>Constraints</h2>
                        {constraints.map((constraint, index) => (
                            <div key={index} className="constraint-item">
                                <h3>{constraint.name}</h3>
                                <p>Sets: {constraint.sets.join(', ')}</p>
                                <p>Params: {constraint.params.join(', ')}</p>
                                <p>Description: {constraint.description}</p>
                            </div>
                        ))}
                    </div>

                    <div className="preferences-section">
                        <h2>Preferences</h2>
                        {preferences.map((preference, index) => (
                            <div key={index} className="preference-item">
                                <h3>{preference.name}</h3>
                                <p>Sets: {preference.sets.join(', ')}</p>
                                <p>Params: {preference.params.join(', ')}</p>
                                <p>Description: {preference.description}</p>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Right Section: Variables */}
                <div className="right-section">
                    <h2>Variables</h2>
                    <div className="variables-list">
                        <div className="variable-item">
                            <h3>People</h3>
                            <ul>
                                {people.map((person, index) => (
                                    <li key={index}>
                                        <input type="checkbox" checked />
                                        {person}
                                    </li>
                                ))}
                            </ul>
                            <button className="add-button">+</button>
                        </div>

                        <div className="variable-item">
                            <h3>Dates</h3>
                            <ul>
                                {dates.map((date, index) => (
                                    <li key={index}>{date}</li>
                                ))}
                            </ul>
                            <button className="add-button">+</button>
                        </div>

                        <div className="variable-item">
                            <h3>Stations</h3>
                            <ul>
                                {stations.map((station, index) => (
                                    <li key={index}>{station}</li>
                                ))}
                            </ul>
                            <button className="add-button">+</button>
                        </div>
                    </div>

                    <div className="base-salary-rate">
                        <h3>Base Salary Rate:</h3>
                        <input
                            type="number"
                            value={baseSalaryRate}
                            onChange={(e) => setBaseSalaryRate(e.target.value)}
                        />
                    </div>
                </div>
            </div>

            {/* Shifts Table Section */}
            <div className="shifts-section">
                <h2>Shifts</h2>
                <table className="shifts-table">
                    <thead>
                        <tr>
                            <th>Station / Date</th>
                            <th>21/12/2024</th>
                            <th>22/12/2024</th>
                            <th>23/12/2024</th>
                        </tr>
                    </thead>
                    <tbody>
                        {['Station 1', 'Station 2', 'Station 3'].map((station) => (
                            <tr key={station}>
                                <td>{station}</td>
                                {[...Array(3)].map((_, index) => (
                                    <td key={index}>
                                        <p>8:00 - Dani</p>
                                        <p>16:00 - Yosi</p>
                                        <p>00:00 - Aharon</p>
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Salaries Table Section */}
            <div className="salaries-section">
                <h2>Salaries</h2>
                <table className="salaries-table">
                    <thead>
                        <tr>
                            <th>Person</th>
                            <th>Salary</th>
                        </tr>
                    </thead>
                    <tbody>
                        {salaries.map((salary, index) => (
                            <tr key={index}>
                                <td>{salary.person}</td>
                                <td>{salary.salary}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Action Buttons */}
            <div className="action-buttons">
                <button className="create-image-button">Create Image</button>
                <button className="solve-button">Solve</button>
            </div>

            <Link to="/" className="back-button">Back</Link>
        </div>
    );
};

export default SolutionPreviewPage;
