import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "./ConfigureVariablesPage.css";

const ConfigureVariablesPage = () => {
  // State for checkboxes
  const [envolvedSets, setEnvolvedSets] = useState({
    people: true,
    dates: true,
    times: false,
    stations: true,
  });

  const [envolvedParams, setEnvolvedParams] = useState({
    shiftTimeIntervals: true,
    extraTimeRate: false,
    baseSalaryRate: true,
  });

  const [parsedVariables, setParsedVariables] = useState({
    shifts: true,
    salaries: false,
  });

  const navigate = useNavigate();

  // Handle checkbox changes
  const handleCheckboxChange = (category, key) => {
    if (category === "sets") {
      setEnvolvedSets((prev) => ({
        ...prev,
        [key]: !prev[key],
      }));
    } else if (category === "params") {
      setEnvolvedParams((prev) => ({
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
    // Navigate to the next page
    navigate("/next-page"); // Replace with your actual next page route
  };

  return (
    <div className="configure-variables-page">
      <h1 className="page-title">Configure Variables of Interest</h1>

      <div className="config-section">
        <h2>Envolved Sets</h2>
        {Object.keys(envolvedSets).map((key) => (
          <div key={key} className="checkbox-item">
            <input
              type="checkbox"
              checked={envolvedSets[key]}
              onChange={() => handleCheckboxChange("sets", key)}
            />
            <label>
              {key.charAt(0).toUpperCase() + key.slice(1).replace("_", " ")}
            </label>
          </div>
        ))}
      </div>

      <div className="config-section">
        <h2>Envolved Params</h2>
        {Object.keys(envolvedParams).map((key) => (
          <div key={key} className="checkbox-item">
            <input
              type="checkbox"
              checked={envolvedParams[key]}
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

      <button
        className="continue-button"
        onClick={() => navigate("/configure-constraints")}
      >
        Continue
      </button>

      <Link to="/" className="back-button">
        Back
      </Link>
    </div>
  );
};

export default ConfigureVariablesPage;
