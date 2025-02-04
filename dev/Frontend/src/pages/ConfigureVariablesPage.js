import React, { useState, useEffect } from "react";
import { Link } from 'react-router-dom';
import { useZPL } from '../context/ZPLContext';
import './ConfigureVariablesPage.css';
import Checkbox from'../reusableComponents/Checkbox.js';

const ConfigureVariablesPage = () => {
    const { variables } = useZPL();

    const allSets = variables.flatMap(variable => variable.dep?.setDependencies ?? []);
    const allParams = variables.flatMap(variable => variable.dep?.paramDependencies ?? []);

    const [selectedVars, setSelectedVars] = useState([]);
    const [selectedSets, setSelectedSets] = useState([]);
    const [selectedParams, setSelectedParams] = useState([]);
    const [displaySets, setDisplaySets] = useState([]);
    const [displayParams, setDisplayParams] = useState([]);

    const determineSetAndParamsToDisplay = () => {
        const newDisplaySets = selectedVars
    .flatMap(variable => variable.dep?.setDependencies ?? [])
    .reduce((unique, item) => 
        unique.includes(item) ? unique : [...unique, item], 
    []);

    const newDisplayParams = selectedVars
    .flatMap(variable => variable.dep?.paramDependencies ?? [])
    .reduce((unique, item) => 
        unique.includes(item) ? unique : [...unique, item], 
    []);
    
        setDisplaySets(newDisplaySets);
        setDisplayParams(newDisplayParams);
    };

    useEffect(() => {
        determineSetAndParamsToDisplay();
    }, [selectedVars]);
    

    const handleVarCheckboxChange = (itemId) => {
        setSelectedVars(prevSelectedVars => {
        
          if (prevSelectedVars.includes(itemId)) {
            return prevSelectedVars.filter(id => id !== itemId);
          } else {
            return [...prevSelectedVars, itemId];
          }
        });
        determineSetAndParamsToDisplay();
      };

    const handleSetsCheckboxChange = (itemId) => {
        setSelectedSets(prevSelected => {
          if (prevSelected.includes(itemId)) {
            return prevSelected.filter(id => id !== itemId);
          } else {
            return [...prevSelected, itemId];
          }
        });

      };

    const handleParamsCheckboxChange = (itemId) => {
        setSelectedParams(prevSelected => { 
          if (prevSelected.includes(itemId)) {
            return prevSelected.filter(id => id !== itemId);
          } else {
            return [...prevSelected, itemId];
          }
        });

      };

    return (
        <div className="configure-variables-page">
            <h1 className="page-title">Configure Variables</h1>
            <div className="variables-layout">
                
                {/* Variables Section */}
                <div className="available-variables">
                    <h2>Available Variables</h2>
                    {variables && Array.isArray(variables) && variables.length > 0 ? (
                        variables.map((variable, index) => (
                            // <div key={index} className="variable-box">
                            //     {variable.identifier}
                            // </div>
                            <Checkbox
                                className="variable-box"
                                key={index}
                                label={variable.identifier}
                                checked={selectedVars.includes(variable)}
                                onChange={() => handleVarCheckboxChange(variable)}
                            />
                        ))
                    ) : (
                        <p>No variables available.</p>
                    )}
                </div>
                
                {/* Sets & Parameters Section */}
                <div className="involved-section">
                    <h2>All Sets</h2>
                    {displaySets.length > 0 ? (
                        displaySets.map((set, index) => (
                            // <div key={index} className="set-box">
                            //     {set}
                            // </div>
                            <Checkbox
                                className="set-box"
                                key={index}
                                label={set}
                                checked={selectedSets.includes(set)}
                                onChange={() => handleSetsCheckboxChange(set)}
                            />
                        ))
                    ) : (
                        <p>No sets available.</p>
                    )}
                    
                    <h2>All Parameters</h2>
                    {displayParams.length > 0 ? (
                        displayParams.map((param, index) => (
                            // <div key={index} className="param-box">
                            //     {param}
                            // </div>
                            <Checkbox
                                className="param-box"
                                key={index}
                                label={param}
                                checked={selectedParams.includes(param)}
                                onChange={() => handleParamsCheckboxChange(param)}
                            />
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
