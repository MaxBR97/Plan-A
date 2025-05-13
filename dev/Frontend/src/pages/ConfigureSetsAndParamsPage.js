import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureSetsAndParamsPage.css";

const ConfigureSetsAndParamsPage = () => {
  const { image, updateImageField } = useZPL();
  
  // Initialize state for sets and params from all modules
  const [allSets, setAllSets] = useState([]);
  const [allParams, setAllParams] = useState([]);
  
  // State for edited values
  const [editedSets, setEditedSets] = useState({});
  const [editedParams, setEditedParams] = useState({});

  useEffect(() => {
    // Collect all sets and parameters from all modules
    const sets = [];
    const params = [];
    
    // Add sets and params from variablesModule
    if (image.variablesModule) {
      if (image.variablesModule.inputSets) {
        sets.push(...image.variablesModule.inputSets);
      }
      if (image.variablesModule.inputParams) {
        params.push(...image.variablesModule.inputParams);
      }
    }
    
    // Add sets and params from constraintModules
    if (image.constraintModules) {
      image.constraintModules.forEach(module => {
        if (module.inputSets) {
          sets.push(...module.inputSets);
        }
        if (module.inputParams) {
          params.push(...module.inputParams);
        }
      });
    }
    
    // Add sets and params from preferenceModules
    if (image.preferenceModules) {
      image.preferenceModules.forEach(module => {
        if (module.inputSets) {
          sets.push(...module.inputSets);
        }
        if (module.inputParams) {
          params.push(...module.inputParams);
        }
        if (module.costParams) {
          params.push(...module.costParams);
        }
      });
    }
    
    setAllSets(sets);
    setAllParams(params);
    
    // Initialize edited values with current values
    const initialSets = {};
    sets.forEach(set => {
      initialSets[set.name] = {
        tags: Array.isArray(set.tags) ? [...set.tags] : (set.type ? [...set.type] : []),
        alias: set.alias || set.name
      };
    });
    
    const initialParams = {};
    params.forEach(param => {
      initialParams[param.name] = {
        tag: param.tag || param.type,
        alias: param.alias || param.name
      };
    });
    
    setEditedSets(initialSets);
    setEditedParams(initialParams);
  }, [image]);

  // Handle changes to set tags
  const handleSetTagChange = (setName, tagIndex, value) => {
    setEditedSets(prev => {
      const updatedSet = { ...prev[setName] };
      if (!updatedSet.tags) {
        updatedSet.tags = [];
      }
      updatedSet.tags[tagIndex] = value;
      return { ...prev, [setName]: updatedSet };
    });
  };

  // Handle changes to set alias
  const handleSetAliasChange = (setName, value) => {
    setEditedSets(prev => ({
      ...prev,
      [setName]: { ...prev[setName], alias: value }
    }));
  };

  // Handle changes to parameter tag
  const handleParamTagChange = (paramName, value) => {
    setEditedParams(prev => ({
      ...prev,
      [paramName]: { ...prev[paramName], tag: value }
    }));
  };

  // Handle changes to parameter alias
  const handleParamAliasChange = (paramName, value) => {
    setEditedParams(prev => ({
      ...prev,
      [paramName]: { ...prev[paramName], alias: value }
    }));
  };

  // Save changes and update the image DTO
  const handleContinue = () => {
    // Update all sets in the image
    const updatedImage = { ...image };
    
    // Update sets in variablesModule
    if (updatedImage.variablesModule && updatedImage.variablesModule.inputSets) {
      updatedImage.variablesModule.inputSets = updatedImage.variablesModule.inputSets.map(set => ({
        ...set,
        tags: editedSets[set.name]?.tags || set.tags,
        alias: editedSets[set.name]?.alias || set.alias
      }));
    }
    
    // Update params in variablesModule
    if (updatedImage.variablesModule && updatedImage.variablesModule.inputParams) {
      updatedImage.variablesModule.inputParams = updatedImage.variablesModule.inputParams.map(param => ({
        ...param,
        tag: editedParams[param.name]?.tag || param.tag,
        alias: editedParams[param.name]?.alias || param.alias
      }));
    }
    
    // Update sets and params in constraintModules
    if (updatedImage.constraintModules) {
      updatedImage.constraintModules = updatedImage.constraintModules.map(module => {
        const updatedModule = { ...module };
        
        if (updatedModule.inputSets) {
          updatedModule.inputSets = updatedModule.inputSets.map(set => ({
            ...set,
            tags: editedSets[set.name]?.tags || set.tags,
            alias: editedSets[set.name]?.alias || set.alias
          }));
        }
        
        if (updatedModule.inputParams) {
          updatedModule.inputParams = updatedModule.inputParams.map(param => ({
            ...param,
            tag: editedParams[param.name]?.tag || param.tag,
            alias: editedParams[param.name]?.alias || param.alias
          }));
        }
        
        return updatedModule;
      });
    }
    
    // Update sets and params in preferenceModules
    if (updatedImage.preferenceModules) {
      updatedImage.preferenceModules = updatedImage.preferenceModules.map(module => {
        const updatedModule = { ...module };
        
        if (updatedModule.inputSets) {
          updatedModule.inputSets = updatedModule.inputSets.map(set => ({
            ...set,
            tags: editedSets[set.name]?.tags || set.tags,
            alias: editedSets[set.name]?.alias || set.alias
          }));
        }
        
        if (updatedModule.inputParams) {
          updatedModule.inputParams = updatedModule.inputParams.map(param => ({
            ...param,
            tag: editedParams[param.name]?.tag || param.tag,
            alias: editedParams[param.name]?.alias || param.alias
          }));
        }
        
        if (updatedModule.costParams) {
          updatedModule.costParams = updatedModule.costParams.map(param => ({
            ...param,
            tag: editedParams[param.name]?.tag || param.tag,
            alias: editedParams[param.name]?.alias || param.alias
          }));
        }
        
        return updatedModule;
      });
    }
    
    // Update the image
    updateImageField("image", updatedImage);
  };

  return (
    <div className="configure-sets-params-page">
      <h1 className="page-title">Configure Sets and Parameters</h1>
      
      {/* Sets Section */}
      <section className="sets-section">
        <h2>Sets</h2>
        {allSets.length > 0 ? (
          <div className="items-container">
            {allSets.map((set, index) => {
              const typeLength = Array.isArray(set.type) ? set.type.length : 0;
              return (
                <div key={`set-${index}`} className="item-card">
                  <h3>{set.name}</h3>
                  
                  <div className="field-group">
                    <label>Alias:</label>
                    <input
                      type="text"
                      value={editedSets[set.name]?.alias || set.name}
                      onChange={(e) => handleSetAliasChange(set.name, e.target.value)}
                    />
                  </div>
                  
                  <div className="tags-group">
                    <h4>Tags:</h4>
                    {Array.from({ length: typeLength }).map((_, tagIndex) => (
                      <div key={`set-${index}-tag-${tagIndex}`} className="tag-input">
                        <label>Tag {tagIndex + 1}:</label>
                        <input
                          type="text"
                          value={(editedSets[set.name]?.tags && editedSets[set.name]?.tags[tagIndex]) || 
                                 (set.type && set.type[tagIndex]) || 
                                 ""}
                          onChange={(e) => handleSetTagChange(set.name, tagIndex, e.target.value)}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <p className="no-data-message">No sets available.</p>
        )}
      </section>
      
      {/* Parameters Section */}
      <section className="params-section">
        <h2>Parameters</h2>
        {allParams.length > 0 ? (
          <div className="items-container">
            {allParams.map((param, index) => (
              <div key={`param-${index}`} className="item-card">
                <h3>{param.name}</h3>
                
                <div className="field-group">
                  <label>Alias:</label>
                  <input
                    type="text"
                    value={editedParams[param.name]?.alias || param.name}
                    onChange={(e) => handleParamAliasChange(param.name, e.target.value)}
                  />
                </div>
                
                <div className="field-group">
                  <label>Tag:</label>
                  <input
                    type="text"
                    value={editedParams[param.name]?.tag || param.type}
                    onChange={(e) => handleParamTagChange(param.name, e.target.value)}
                  />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="no-data-message">No parameters available.</p>
        )}
      </section>
      
      <div className="navigation-buttons">
        <Link to="/configure-preferences" className="continue-button" onClick={handleContinue}>
          Continue
        </Link>
        <Link to="/" className="back-button">
          Back
        </Link>
      </div>
    </div>
  );
};

export default ConfigureSetsAndParamsPage;