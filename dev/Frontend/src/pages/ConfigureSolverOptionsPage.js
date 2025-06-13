import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureSolverOptionsPage.css";

// Configuration flag to enable/disable custom scripts section
const ENABLE_CUSTOM_SCRIPTS = true;

const ConfigureSolverOptionsPage = () => {
  const { image, updateImageField } = useZPL();
  console.log(image.solverSettings);

  const predefinedSettings = {
    Default: "",
    Optimallity: "set emphasis optimality",
    "Tree search": "set emphasis cpsolver",
    Feasibility: "set emphasis feasibility",
    "Aggressive static analysis": "set presolving emphasis aggressive",
    Numerics: "set emphasis numerics"
  };

  const settingDescriptions = {
    Default: "Default solver behavior without any specific emphasis.",
    Optimallity: "Emphasizes proving optimality fast, potentially at the cost of reaching good suboptimal solutions slower.",
    "Tree search": "Emphasizes tree search, suitable for highly constrained search problems.",
    Feasibility: "Emphasizes finding feasiblility.",
    "Aggressive static analysis": "Emphasizes static analysis of the problem before solving, potentially turncating significantly large parts of the search tree.",
    Numerics: "Emphasizes numerical stability and precision during the solution process."
  };

  const [selectedPredefined, setSelectedPredefined] = useState({});
  const [customScripts, setCustomScripts] = useState({});
  const [scriptOrder, setScriptOrder] = useState([]);
  const [scriptNameEdits, setScriptNameEdits] = useState({});

  useEffect(() => {
    const existingSettings = image?.solverSettings || {};
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
    setScriptNameEdits(customSelected);
  }, []);

  const handlePredefinedChange = (settingName) => {
    const updatedSelection = { ...selectedPredefined };

    if (settingName in updatedSelection) {
      delete updatedSelection[settingName];
    } else {
      updatedSelection[settingName] = predefinedSettings[settingName];
    }

    const finalSettings = Object.keys(updatedSelection).length === 0
      ? { default: "" }
      : updatedSelection;

    setSelectedPredefined(updatedSelection);
    updateSolverSettings({ ...finalSettings, ...customScripts });
  };

  const handleAddCustomScript = () => {
    const newName = `custom${Object.keys(customScripts).length + 1}`;
    const updatedScripts = { ...customScripts, [newName]: "" };
    const updatedEdits = { ...scriptNameEdits, [newName]: newName };

    setCustomScripts(updatedScripts);
    setScriptOrder(prev => [...prev, newName]);
    setScriptNameEdits(updatedEdits);
  };

  const handleRemoveCustomScript = (scriptName) => {
    const updatedScripts = { ...customScripts };
    delete updatedScripts[scriptName];

    const updatedEdits = { ...scriptNameEdits };
    delete updatedEdits[scriptName];

    setCustomScripts(updatedScripts);
    setScriptOrder(prev => prev.filter(name => name !== scriptName));
    setScriptNameEdits(updatedEdits);

    const finalSettings = Object.keys(selectedPredefined).length === 0 && Object.keys(updatedScripts).length === 0
      ? { default: "" }
      : { ...selectedPredefined, ...updatedScripts };

    updateSolverSettings(finalSettings);
  };

  const handleCustomNameChange = (oldName, newName) => {
    if (!newName.trim() || customScripts[newName]) return;

    const updatedScripts = { ...customScripts };
    updatedScripts[newName] = updatedScripts[oldName];
    delete updatedScripts[oldName];

    const updatedEdits = { ...scriptNameEdits };
    delete updatedEdits[oldName];
    updatedEdits[newName] = newName;

    setCustomScripts(updatedScripts);
    setScriptNameEdits(updatedEdits);
    setScriptOrder(prev => prev.map(name => name === oldName ? newName : name));

    updateSolverSettings({ ...selectedPredefined, ...updatedScripts });
  };

  const handleCustomScriptChange = (name, value) => {
    const updatedScripts = { ...customScripts, [name]: value };
    setCustomScripts(updatedScripts);
    updateSolverSettings({ ...selectedPredefined, ...updatedScripts });
  };

  const updateSolverSettings = (newSettings) => {
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
          Select relevant solver settings for your model. Some might be relevant more than others, depending on your model.
        </p>
        <div className="predefined-options">
          {Object.entries(predefinedSettings).map(([settingName]) => (
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
                    value={scriptNameEdits[scriptName] ?? scriptName}
                    onChange={(e) =>
                      setScriptNameEdits(prev => ({
                        ...prev,
                        [scriptName]: e.target.value
                      }))
                    }
                    onBlur={() => {
                      const newName = scriptNameEdits[scriptName];
                      if (newName && newName !== scriptName) {
                        handleCustomNameChange(scriptName, newName);
                      }
                    }}
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
