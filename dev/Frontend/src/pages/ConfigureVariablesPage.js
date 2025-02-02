import React from 'react';
import { Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureVariablesPage.css';

const ConfigureVariablesPage = () => {
    const { variables } = useZPL();
    
    console.log("Variables in Context:", variables);

    const allSets = variables.flatMap(variable => variable.dep?.setDependencies ?? []);
    const allParams = variables.flatMap(variable => variable.dep?.paramDependencies ?? []);

    return (
        <div className="configure-variables-page">
            <h1 className="page-title">Configure Variables</h1>
            <div className="variables-layout">
                
                {/* Variables Section */}
                <div className="available-variables">
                    <h2>Available Variables</h2>
                    {variables && Array.isArray(variables) && variables.length > 0 ? (
                        variables.map((variable, index) => (
                            <div key={index} className="variable-box">
                                {variable.identifier}
                            </div>
                        ))
                    ) : (
                        <p>No variables available.</p>
                    )}
                </div>
                
                {/* Sets & Parameters Section */}
                <div className="involved-section">
                    <h2>All Sets</h2>
                    {allSets.length > 0 ? (
                        allSets.map((set, index) => (
                            <div key={index} className="set-box">
                                {set}
                            </div>
                        ))
                    ) : (
                        <p>No sets available.</p>
                    )}
                    
                    <h2>All Parameters</h2>
                    {allParams.length > 0 ? (
                        allParams.map((param, index) => (
                            <div key={index} className="param-box">
                                {param}
                            </div>
                        ))
                    ) : (
                        <p>No parameters available.</p>
                    )}
                </div>
            </div>
            
            <Link to="/configure-constraints" className="continue-button">Continue</Link>
        </div>
    );
};

export default ConfigureVariablesPage;
