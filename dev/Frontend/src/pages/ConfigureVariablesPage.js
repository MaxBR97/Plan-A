import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useZPL } from "../context/ZPLContext"; // Import context
import "./ConfigureVariablesPage.css";

const ConfigureVariablesPage = () => {
  const { imageId, variables, types, constraints, preferences } = useZPL(); // Get stored JSON data
  
  // Extracting sets from types
  const involvedSetsInitial = types?.sets ? Object.keys(types.sets).reduce((acc, key) => {
    acc[key] = false; // Default all checkboxes to false
    return acc;
  }, {}) : {};

  // Extracting params from types
  const involvedParamsInitial = types?.params ? Object.keys(types.params).reduce((acc, key) => {
    acc[key] = false; // Default all checkboxes to false
    return acc;
  }, {}) : {};

  // Extracting variables
  const parsedVariablesInitial = variables && Array.isArray(variables) ? variables.reduce((acc, key) => {
    acc[key] = false; // Default all checkboxes to false
    return acc;
  }, {}) : {};

  useEffect(() => {
    // Alert the JSON content when the page loads
    alert(`This is the configure Variable Page - JSON Data:\n${JSON.stringify({ imageId, variables, types, constraints, preferences }, null, 2)}`);
  }, [imageId, variables, types, constraints, preferences]);

  // State for checkboxes
  const [involvedSets, setInvolvedSets] = useState(involvedSetsInitial);
  const [involvedParams, setInvolvedParams] = useState(involvedParamsInitial);
  const [parsedVariables, setParsedVariables] = useState(parsedVariablesInitial);

  const navigate = useNavigate();

  // Handle checkbox changes
  const handleCheckboxChange = (category, key) => {
    if (category === "sets") {
      setInvolvedSets((prev) => ({
        ...prev,
        [key]: !prev[key],
      }));
    } else if (category === "params") {
      setInvolvedParams((prev) => ({
        ...prev,
        [key]: !prev[key],
      }));
    } else if (category === "variables") {
      setParsedVariables((prev) => ({
        ...prev,
        [key]: !prev[key],
      }));
    }
  };

  // Handle Continue button click
  const handleContinue = () => {
    navigate("/configure-constraints"); // Replace with your actual next page route
  };

  return (
    <div className="configure-variables-page">
      <h1 className="page-title">Configure Variables of Interest</h1>

      <div className="config-section">
        <h2>Involved Sets</h2>
        {Object.keys(involvedSets).map((key) => (
          <div key={key} className="checkbox-item">
            <input
              type="checkbox"
              checked={involvedSets[key]}
              onChange={() => handleCheckboxChange("sets", key)}
            />
            <label>
              {key.charAt(0).toUpperCase() + key.slice(1).replace("_", " ")}
            </label>
          </div>
        ))}
      </div>

      <div className="config-section">
        <h2>Involved Params</h2>
        {Object.keys(involvedParams).map((key) => (
          <div key={key} className="checkbox-item">
            <input
              type="checkbox"
              checked={involvedParams[key]}
              onChange={() => handleCheckboxChange("params", key)}
            />
            <label>
              {key.charAt(0).toUpperCase() + key.slice(1).replace("_", " ")}
            </label>
          </div>
        ))}
      </div>

      <div className="config-section">
        <h2>Parsed Variables</h2>
        {Object.keys(parsedVariables).map((key) => (
          <div key={key} className="checkbox-item">
            <input
              type="checkbox"
              checked={parsedVariables[key]}
              onChange={() => handleCheckboxChange("variables", key)}
            />
            <label>
              {key.charAt(0).toUpperCase() + key.slice(1).replace("_", " ")}
            </label>
          </div>
        ))}
      </div>

      <button className="continue-button" onClick={handleContinue}>
        Continue
      </button>

      <Link to="/" className="back-button">
        Back
      </Link>
    </div>
  );
};

export default ConfigureVariablesPage;
