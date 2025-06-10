import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useZPL } from "../context/ZPLContext";
import "./ConfigureSetsAndParamsPage.css";
import InfoIcon from '../reusableComponents/InfoIcon';

const ConfigureSetsAndParamsPage = () => {
  const { image, updateImage} = useZPL();
  
  // Initialize state for sets and params from all modules
  const [allSets, setAllSets] = useState(() => {
    const sets = new Set();
    
    // Add sets from variablesModule
    if (image.variablesModule?.inputSets) {
      image.variablesModule.inputSets.forEach(set => {
        // Only add if not already present (using name as unique identifier)
        if (!Array.from(sets).some(existingSet => existingSet.name == set.name)) {
          sets.add(set);
        }
      });
    }
    
    // Add sets from constraintModules
    if (image.constraintModules) {
      image.constraintModules.forEach(module => {
        if (module.inputSets) {
          module.inputSets.forEach(set => {
            if (!Array.from(sets).some(existingSet => existingSet.name === set.name)) {
              sets.add(set);
            }
          });
        }
      });
    }
    
    // Add sets from preferenceModules
    if (image.preferenceModules) {
      image.preferenceModules.forEach(module => {
        if (module.inputSets) {
          module.inputSets.forEach(set => {
            if (!Array.from(sets).some(existingSet => existingSet.name === set.name)) {
              sets.add(set);
            }
          });
        }
      });
    }
    
    return Array.from(sets);
  });

  const [allParams, setAllParams] = useState(() => {
    const params = new Set();
    // Add params from variablesModule
    if (image.variablesModule?.inputParams) {
      image.variablesModule.inputParams.forEach(param => {
        if (!Array.from(params).some(existingParam => existingParam.name === param.name)) {
          params.add(param);
        }
      });
    }
    
    // Add params from constraintModules
    if (image.constraintModules) {
      image.constraintModules.forEach(module => {
        if (module.inputParams) {
          module.inputParams.forEach(param => {
            if (!Array.from(params).some(existingParam => existingParam.name === param.name)) {
              params.add(param);
            }
          });
        }
      });
    }
    
    // Add params from preferenceModules
    if (image.preferenceModules) {
      image.preferenceModules.forEach(module => {
        if (module.inputParams) {
          module.inputParams.forEach(param => {
            if (!Array.from(params).some(existingParam => existingParam.name === param.name)) {
              params.add(param);
            }
          });
        }
        if (module.costParams) {
          module.costParams.forEach(param => {
            if (!Array.from(params).some(existingParam => existingParam.name === param.name)) {
              params.add(param);
            }
          });
        }
      });
    }
    
    return Array.from(params);
  });
  
  // State for edited values
  const [editedSets, setEditedSets] = useState(() => {
    const initialSets = {};
    allSets.forEach(set => {
      initialSets[set.name] = {
        tags: Array.isArray(set.tags) ? [...set.tags] : (Array.isArray(set.type) ? [...set.type] : []),
        alias: set.alias || set.name,
        type: Array.isArray(set.type) ? [...set.type] : []
      };
    });
    return initialSets;
  });

  const [editedParams, setEditedParams] = useState(() => {
    const initialParams = {};
    allParams.forEach(param => {
      initialParams[param.name] = {
        tag: param.tag || param.type || '',
        alias: param.alias || param.name,
        type: param.type || ''
      };
    });
    return initialParams;
  });

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
    
    const dedupedSets = Array.from(
      new Map(sets.map(set => [set.name, set])).values()
    );
  
    const dedupedParams = Array.from(
      new Map(params.map(param => [param.name, param])).values()
    );
  
    setAllSets(dedupedSets);
    setAllParams(dedupedParams);
    
    // Initialize edited values with current values
    const initialSets = {};
    sets.forEach(set => {
      initialSets[set.name] = {
        tags: Array.isArray(set.tags) ? [...set.tags] : (Array.isArray(set.type) ? [...set.type] : []),
        alias: set.alias || set.name,
        type: Array.isArray(set.type) ? [...set.type] : []
      };
    });
    
    const initialParams = {};
    params.forEach(param => {
      initialParams[param.name] = {
        tag: param.tag || param.type || '',
        alias: param.alias || param.name,
        type: param.type || ''
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
    
    
    updateImage( updatedImage);
  };

  return (
    <div className="configure-sets-params-page">
      <h1 className="page-title">Configure Sets and Parameters</h1>
      
      <p className="page-description">
        Customize how your original model's input data will be called to be more readable and meaningful.
      </p>
      
      <div className="sets-params-layout">
        <div className="sets-section">
          <div className="panel-header">
            <h2>
              <div></div>
              <span>Sets</span>
              <InfoIcon tooltip="Define aliases for your sets. These aliases are how the sets will be called in the Image. Name each column to indicate what it represents." />
            </h2>
          </div>
          {allSets.length > 0 ? (
            <div className="items-container">
              {allSets.map((set, index) => {
                const typeLength = Array.isArray(set.type) ? set.type.length : 0;
                return (
                  <div key={`set-${index}`} className="item-card">
                    <div className="original-name">
                      <label>Original set:</label>
                      <span className="name-value">{set.name}</span>
                    </div>
                    
                    <div className="alias-group">
                      <label>Alias:</label>
                      <input
                        type="text"
                        value={editedSets[set.name]?.alias || set.name}
                        onChange={(e) => handleSetAliasChange(set.name, e.target.value)}
                      />
                    </div>
                    
                    {typeLength > 0 && (
                      <div className="tag-inputs-container">
                        <label>Columns:</label>
                        <div className="tag-inputs-row">
                          {Array.from({ length: typeLength }).map((_, tagIndex) => (
                            <div key={`set-${index}-tag-${tagIndex}`} className="tag-input-group">
                              <div className="tag-type-label">{set.type[tagIndex]}</div>
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
                    )}
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="no-data-message">No sets available.</p>
          )}
        </div>

        <div className="params-section">
          <div className="panel-header">
            <h2>
              <div></div>
              <span>Parameters</span>
              <InfoIcon tooltip="Name your paramters with aliases. These aliases are how the parameters will be called in the Image." />
            </h2>
          </div>
          {allParams.length > 0 ? (
            <div className="items-container">
              {allParams.map((param, index) => (
                <div key={`param-${index}`} className="item-card">
                  <div className="original-name">
                    <label>Original parameter:</label>
                    <span className="name-value">{param.name}</span>
                  </div>
                  
                  <div className="alias-group">
                    <label>Alias:</label>
                    <input
                      type="text"
                      value={editedParams[param.name]?.alias || param.name}
                      onChange={(e) => handleParamAliasChange(param.name, e.target.value)}
                    />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="no-data-message">No parameters available.</p>
          )}
        </div>
      </div>
      
      <div className="navigation-buttons">
        <Link to="/configuration-menu" className="continue-button" onClick={handleContinue}>
          Continue
        </Link>
        {/* <Link to="/configuration-menu" className="back-button">
          Back
        </Link> */}
      </div>
    </div>
  );
};

export default ConfigureSetsAndParamsPage;