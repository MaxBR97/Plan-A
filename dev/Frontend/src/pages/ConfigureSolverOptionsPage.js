import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureSolverOptionsPage.css";

// Configuration flag to enable/disable custom scripts section
const ENABLE_CUSTOM_SCRIPTS = false;

const ConfigureSolverOptionsPage = () => {
  const { image, updateImageField } = useZPL();
console.log(image.solverSettings)
  const predefinedSettings = {
    Default: "",
    Optimallity: "set emphasis optimality",
    "Tree search": "set emphasis cpsolver",
    Feasibility: "set emphasis feasibility",
    "Aggressive static analysis": "set presolving emphasis aggressive",
    Numerics: "set emphasis numerics"
  };

  // Descriptions for each predefined setting
  const settingDescriptions = {
    Default: "Default solver behavior without any specific emphasis.",
    Optimallity: "Emphasizes proving optimality fast, potentially at the cost of reaching good suboptimal solutions slower.",
    "Tree search": "Emphasizes tree search, suitable for highly constrained search problems.",
    Feasibility: "Emphasizes finding feasiblility.",
    "Aggressive static analysis": "Emphasizes static analysis of the problem before solving, potentially turncating significantly large parts of the search tree.",
    Numerics: "Emphasizes numerical stability and precision during the solution process."
  };

  // Initialize states
  const [selectedPredefined, setSelectedPredefined] = useState({});
  const [customScripts, setCustomScripts] = useState({});
  // Keep track of script order
  const [scriptOrder, setScriptOrder] = useState([]);

  // Initialize from existing solverSettings if any
  useEffect(() => {
    const existingSettings = image?.solverSettings || {};
    
    // Separate existing settings into predefined and custom
    const predefinedSelected = {};
    const customSelected = {};
    const newScriptOrder = [];
    
    Object.entries(existingSettings).forEach(([key, value]) => {
      if (key in predefinedSettings) {
        
          predefinedSelected[key] = value;
        
      } else {
        customSelected[key] = value;
        newScriptOrder.push(key);
      }
    });

    setSelectedPredefined(predefinedSelected);
    setCustomScripts(customSelected);
    setScriptOrder(newScriptOrder);
  }, []);

  // Handle checkbox changes for predefined settings
  const handlePredefinedChange = (settingName) => {
    const updatedSelection = { ...selectedPredefined };
    
    if (settingName in updatedSelection) {
      delete updatedSelection[settingName];
    } else {
      updatedSelection[settingName] = predefinedSettings[settingName];
    }
    
    // If no settings are selected, ensure we have the default empty setting
    const finalSettings = Object.keys(updatedSelection).length === 0 
      ? { default: "" } 
      : updatedSelection;
    
    setSelectedPredefined(updatedSelection);
    updateSolverSettings({ ...finalSettings, ...customScripts });
  };

  // Handle adding a new custom script
  const handleAddCustomScript = () => {
    const newName = `custom${Object.keys(customScripts).length + 1}`;
    setCustomScripts(prev => ({ ...prev, [newName]: "" }));
    setScriptOrder(prev => [...prev, newName]);
  };

  // Handle removing a custom script
  const handleRemoveCustomScript = (scriptName) => {
    const updatedScripts = { ...customScripts };
    delete updatedScripts[scriptName];
    setCustomScripts(updatedScripts);
    setScriptOrder(prev => prev.filter(name => name !== scriptName));
    
    // If no settings are selected and no custom scripts, ensure we have the default empty setting
    const finalSettings = Object.keys(selectedPredefined).length === 0 && Object.keys(updatedScripts).length === 0
      ? { default: "" }
      : { ...selectedPredefined, ...updatedScripts };
    
    updateSolverSettings(finalSettings);
  };

  // Handle custom script name change
  const handleCustomNameChange = (oldName, newName) => {
    if (newName.trim() === "") return;
    if (oldName !== newName && !customScripts[newName]) {
      const updatedScripts = { ...customScripts };
      updatedScripts[newName] = updatedScripts[oldName];
      delete updatedScripts[oldName];
      setCustomScripts(updatedScripts);
      
      // Update the order array with the new name
      setScriptOrder(prev => prev.map(name => name === oldName ? newName : name));
      
      updateSolverSettings({ ...selectedPredefined, ...updatedScripts });
    }
  };

  // Handle custom script content change
  const handleCustomScriptChange = (name, value) => {
    const updatedScripts = { ...customScripts, [name]: value };
    setCustomScripts(updatedScripts);
    updateSolverSettings({ ...selectedPredefined, ...updatedScripts });
  };

  // Update the image's solverSettings
  const updateSolverSettings = (newSettings) => {
    // Ensure we always have at least the default setting if nothing else is selected
    const finalSettings = Object.keys(newSettings).length === 0 
      ? { default: "" } 
      : newSettings;
    
    updateImageField("solverSettings", finalSettings);
  };

  return (
    <div className="configure-solver-container">
      <section className="predefined-settings-section">
        <h2>Choose Solver Emphasis Settings</h2>
        <p className="section-description">
          Select from our predefined solver settings to optimize your solution process.
        </p>
        <div className="predefined-options">
          {Object.entries(predefinedSettings).map(([settingName, value]) => (
            <div key={settingName} className="setting-option">
              <div className="setting-header">
                <label>
                  <input
                    type="checkbox"
                    checked={settingName in selectedPredefined}
                    onChange={() => handlePredefinedChange(settingName)}
                  />
                  {settingName}
                </label>
              </div>
              <p className="setting-description">
                {settingDescriptions[settingName]}
              </p>
            </div>
          ))}
        </div>
      </section>

      {ENABLE_CUSTOM_SCRIPTS && (
        <section className="custom-scripts-section">
          <h2>Make a Custom Solver Script</h2>
          <p className="section-description">
            Create your own solver settings by defining custom name-script pairs.
          </p>
          
          <div className="custom-scripts-container">
            {scriptOrder.map((scriptName) => (
              <div key={scriptName} className="custom-script-pair">
                <div className="script-header">
                  <input
                    type="text"
                    className="script-name-input"
                    value={scriptName}
                    onChange={(e) => handleCustomNameChange(scriptName, e.target.value)}
                    placeholder="Script Name"
                  />
                  <button 
                    className="remove-script-btn"
                    onClick={() => handleRemoveCustomScript(scriptName)}
                  >
                    ×
                  </button>
                </div>
                <textarea
                  className="script-content-textarea"
                  value={customScripts[scriptName]}
                  onChange={(e) => handleCustomScriptChange(scriptName, e.target.value)}
                  placeholder="Enter solver script here..."
                  rows={4}
                />
              </div>
            ))}
          </div>
          
          <button className="add-script-btn" onClick={handleAddCustomScript}>
            + Add More Script
          </button>
        </section>
      )}

      <div className="navigation-buttons">
        <Link to="/configuration-menu" className="continue-button">
          Continue
        </Link>
      </div>
    </div>
  );
};

export default ConfigureSolverOptionsPage;