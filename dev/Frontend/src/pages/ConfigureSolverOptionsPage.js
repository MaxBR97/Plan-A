import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureSolverOptionsPage.css";

const ConfigureSolverOptionsPage = () => {
  const {
    image,
    model,
    solutionResponse,
    updateImage,
    updateImageField,
    updateModel,
    updateSolutionResponse,
    initialImageState
  } = useZPL();

  // Initialize solverSettings from image or use default if empty
  const [solverSettings, setsolverSettings] = useState(() => {
    const scripts = image?.solverSettings || {};
    
    // If there are no scripts, add the default one
    if (Object.keys(scripts).length === 0) {
      return { "default": "" };
    }
    
    return scripts;
  });

  // Handle adding a new script pair
  const handleAddScript = () => {
    const newName = `script${Object.keys(solverSettings).length + 1}`;
    setsolverSettings({ ...solverSettings, [newName]: "" });
  };

  // Handle removing a script pair
  const handleRemoveScript = (scriptName) => {
    const updatedScripts = { ...solverSettings };
    delete updatedScripts[scriptName];

    // If all scripts were removed, add back the default one
    if (Object.keys(updatedScripts).length === 0) {
      setsolverSettings({ "default": "" });
    } else {
      setsolverSettings(updatedScripts);
    }
  };

  // Handle changing script name
  const handleNameChange = (oldName, newName) => {
    if (newName.trim() === "") return;
    
    if (oldName !== newName && !solverSettings[newName]) {
      const updatedScripts = { ...solverSettings };
      updatedScripts[newName] = updatedScripts[oldName];
      delete updatedScripts[oldName];
      setsolverSettings(updatedScripts);
    }
  };

  // Handle changing script content
  const handleScriptChange = (name, value) => {
    setsolverSettings({ ...solverSettings, [name]: value });
  };

  // Save solverSettings to image when continuing
  const handleContinue = () => {
    updateImageField("solverSettings", solverSettings);
  };

  // Display scripts in a more organized fashion
  const renderScriptPairs = () => {
    return Object.entries(solverSettings).map(([name, script], index) => (
      <div key={index} className="script-pair">
        <div className="script-header">
          <input
            type="text"
            className="script-name-input"
            value={name}
            onChange={(e) => handleNameChange(name, e.target.value)}
            placeholder="Script Name"
          />
          {Object.keys(solverSettings).length > 1 && (
            <button 
              className="remove-script-btn"
              onClick={() => handleRemoveScript(name)}
            >
              ×
            </button>
          )}
        </div>
        <textarea
          className="script-content-textarea"
          value={script}
          onChange={(e) => handleScriptChange(name, e.target.value)}
          placeholder="Enter script content here..."
          rows={5}
        />
      </div>
    ));
  };

  return (
    <div className="configure-variables-container">
      <h1>Configure Solver Settings</h1>
      <p className="page-description">
        Create pairs of solver settings names and their implementations. 
        At least one script must be defined at all times.
      </p>
      
      <div className="scripts-container">
        {renderScriptPairs()}
      </div>
      
      <button className="add-script-btn" onClick={handleAddScript}>
        + Add New Script
      </button>
      
      <div className="navigation-buttons">
        <Link to="/configuration-menu" className="back-button">
          Back
        </Link>
        <Link to="/configuration-menu" className="continue-button" onClick={handleContinue}>
          Continue
        </Link>
      </div>
    </div>
  );
};

export default ConfigureSolverOptionsPage;