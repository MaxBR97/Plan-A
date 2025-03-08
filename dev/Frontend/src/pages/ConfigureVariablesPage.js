import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureVariablesPage.css";
import Checkbox from "../reusableComponents/Checkbox.js";
import TagConfigure from "../reusableComponents/TagConfigure.js";

const ConfigureVariablesPage = () => {
    const { variables, varTypes, variablesModule, setVariablesModule } = useZPL();

    const [selectedVars, setSelectedVars] = useState((variables || []));  // Stores selected variables
    const [displaySets, setDisplaySets] = useState([]);    // Stores sets that should be displayed
    const [displayParams, setDisplayParams] = useState([]); // Stores params that should be displayed
    const [selectedSets, setSelectedSets] = useState([]);  // Stores selected sets
    const [selectedParams, setSelectedParams] = useState([]); // Stores selected params
    const [hasInitialized, setHasInitialized] = useState(false);
    const [variablesTags, setVariablesTags] = useState(varTypes)
   
    useEffect(() => {
        // First, update the displayed sets and params
        const newDisplaySets = selectedVars
          .flatMap(variable => variable.dep?.setDependencies ?? [])
          .reduce((unique, item) => unique.includes(item) ? unique : [...unique, item], []);
      
        const newDisplayParams = selectedVars
          .flatMap(variable => variable.dep?.paramDependencies ?? [])
          .reduce((unique, item) => unique.includes(item) ? unique : [...unique, item], []);
      
        setDisplaySets(newDisplaySets);
        setDisplayParams(newDisplayParams);
        
        // Only during initialization, also set the selected sets and params
        if (!hasInitialized && newDisplaySets.length > 0) {
          setSelectedSets(newDisplaySets);
          setSelectedParams(newDisplayParams);
          setHasInitialized(true);
        } else {
          // For subsequent updates, filter out any that are no longer displayed
          setSelectedSets(prevSelected => 
            prevSelected.filter(set => newDisplaySets.includes(set))
          );
          setSelectedParams(prevSelected => 
            prevSelected.filter(param => newDisplayParams.includes(param))
          );
        }
        
      }, [selectedVars, hasInitialized]);

    // Handles variable selection (checkbox clicked)
    const handleVarCheckboxChange = (variable) => {
        setSelectedVars(prevSelectedVars => {
            if (prevSelectedVars.includes(variable)) {
                return prevSelectedVars.filter(v => v !== variable);
            } else {
                return [...prevSelectedVars, variable];
            }
        });
    };

    // Handles set selection (checkbox clicked)
    const handleSetCheckboxChange = (set) => {
        setSelectedSets(prevSelected => {
            if (prevSelected.includes(set)) {
                return prevSelected.filter(s => s !== set);
            } else {
                return [...prevSelected, set];
            }
        });
    };

    // Handles parameter selection (checkbox clicked)
    const handleParamCheckboxChange = (param) => {
        setSelectedParams(prevSelected => {
            if (prevSelected.includes(param)) {
                return prevSelected.filter(p => p !== param);
            } else {
                return [...prevSelected, param];
            }
        });
    };

    // Save selected variables, sets, and parameters in context when navigating
    const handleContinue = () => {
        setVariablesModule({
            variablesOfInterest: selectedVars.map(v => v.identifier),
            variablesConfigurableSets: selectedSets,
            variablesConfigurableParams: selectedParams,
            variablesTags: variablesTags
        });
    };

    // In your parent component where selectedVars is defined
const handleTagValueChange = (e, varIdentifier, index) => {
    // Make a copy of the current state
    const updatedVariablesTags = { ...variablesTags };
    
    // Update the specific value at the specific index for the specific variable
    updatedVariablesTags[varIdentifier][index] = e.target.value;
    
    // Update the state
    setVariablesTags(updatedVariablesTags);
  };
console.log("variables tags: ",variablesTags)
    return (
        <div className="configure-variables-page">
            <h1 className="page-title">Configure Variables</h1>
            <div className="variables-layout">
                
                {/* Variables Section */}
                <div className="available-variables">
                    <h2>Available Variables</h2>
                    {variables.length > 0 ? (
                        variables.map((variable, index) => (
                            <Checkbox
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
                
                {/* Sets & Parameters Section (Only for Selected Variables) */}
                <div className="involved-section">
                    <h2>Involved Sets</h2>
                    {displaySets.length > 0 ? (
                        displaySets.map((set, index) => (
                            <Checkbox
                                key={index}
                                label={set}
                                checked={selectedSets.includes(set)}
                                onChange={() => handleSetCheckboxChange(set)}
                            />
                        ))
                    ) : (
                        <p>No sets available.</p>
                    )}

                    <h2>Involved Parameters</h2>
                    {displayParams.length > 0 ? (
                        displayParams.map((param, index) => (
                            <Checkbox
                                key={index}
                                label={param}
                                checked={selectedParams.includes(param)}
                                onChange={() => handleParamCheckboxChange(param)}
                            />
                        ))
                    ) : (
                        <p>No parameters available.</p>
                    )}
                </div>
                <div className="tags-container">
                    <h2>Tag Variables' Output Tuple</h2>
                    {selectedVars.map((value, key) => {
                        return <TagConfigure
                        key={key}
                        label="s"
                        variable={value}
                        types={varTypes[value.identifier]}
                        values={variablesTags[value.identifier] || []}
                        onChange={(e, index) => handleTagValueChange(e, value.identifier, index)}
                        />
                    })}
                </div>
            </div>
            
            <Link to="/configure-constraints" className="continue-button" onClick={handleContinue}>
                Continue
            </Link>
        </div>
    );
};

export default ConfigureVariablesPage;
