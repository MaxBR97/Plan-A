import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureVariablesPage.css";

const ConfigureVariablesPage = () => {
    const { types, variables } = useZPL();
    
    const [involvedSetsAndParams, setInvolvedSetsAndParams] = useState({});
    const [parsedVariables, setParsedVariables] = useState({});
    
    useEffect(() => {
        if (types) {
            setInvolvedSetsAndParams(
                Object.keys(types).reduce((acc, key) => {
                    acc[key] = false;
                    return acc;
                }, {})
            );
        }

        if (variables) {
            setParsedVariables(
                variables.reduce((acc, variable) => {
                    acc[variable.identifier] = false;
                    return acc;
                }, {})
            );
        }
    }, [types, variables]);

    const navigate = useNavigate();

    const handleCheckboxChange = (category, key) => {
        if (category === "setsAndParams") {
            setInvolvedSetsAndParams((prev) => ({
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

    const handleContinue = () => {
        navigate("/configure-constraints");
    };

    return (
        <div className="configure-variables-page">
            <h1 className="page-title">Configure Variables of Interest</h1>

            <div className="config-section">
                <h2>Involved Sets and Params</h2>
                {Object.keys(involvedSetsAndParams).map((key) => (
                    <div key={key} className="checkbox-item">
                        <input
                            type="checkbox"
                            checked={involvedSetsAndParams[key]}
                            onChange={() => handleCheckboxChange("setsAndParams", key)}
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
