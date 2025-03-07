import React, { createContext, useContext, useState } from "react";

const ZPLContext = createContext();

// Define initial states outside the component to reuse them in the reset function
const initialState = {
  constraints: [],
  preferences: [],
  modules: [],
  preferenceModules: [],
  variables: [],
  setTypes: {},
  setTags: {},
  paramTypes: {},
  varTypes: {},
  imageId: null,
  solutionResponse: null,
  variablesModule: {
    variablesOfInterest: [],
    variablesConfigurableSets: [],
    variablesConfigurableParams: [],
    variablesTags: [],
  }
};

export const ZPLProvider = ({ children }) => {
  const [constraints, setConstraints] = useState(initialState.constraints);
  const [preferences, setPreferences] = useState(initialState.preferences);
  const [modules, setModules] = useState(initialState.modules);
  const [preferenceModules, setPreferenceModules] = useState(initialState.preferenceModules);
  const [variables, setVariables] = useState(initialState.variables);
  const [setTypes, setSetTypes] = useState(initialState.setTypes);
  const [setTags, setSetTags] = useState(initialState.setTags);
  const [paramTypes, setParamTypes] = useState(initialState.paramTypes);
  const [varTypes, setVarTypes] = useState(initialState.varTypes);
  const [imageId, setImageId] = useState(initialState.imageId);
  const [solutionResponse, setSolutionResponse] = useState(initialState.solutionResponse);
  const [variablesModule, setVariablesModule] = useState(initialState.variablesModule);

  // Reset function that sets all states back to their initial values
  const resetAll = () => {
    setConstraints(initialState.constraints);
    setPreferences(initialState.preferences);
    setModules(initialState.modules);
    setPreferenceModules(initialState.preferenceModules);
    setVariables(initialState.variables);
    setSetTypes(initialState.setTypes);
    setSetTags(initialState.setTags);
    setParamTypes(initialState.paramTypes);
    setVarTypes(initialState.varTypes);
    setImageId(initialState.imageId);
    setSolutionResponse(initialState.solutionResponse);
    setVariablesModule(initialState.variablesModule);
  };

  return (
    <ZPLContext.Provider value={{
      constraints, setConstraints,
      preferences, setPreferences,
      modules, setModules,
      preferenceModules, setPreferenceModules,
      variables, setVariables,
      setTypes, setSetTypes,
      setTags, setSetTags,
      paramTypes, setParamTypes,
      varTypes, setVarTypes,
      imageId, setImageId,
      solutionResponse, setSolutionResponse,
      variablesModule, setVariablesModule,
      resetAll // Add the reset function to the context
    }}>
      {children}
    </ZPLContext.Provider>
  );
};

export const useZPL = () => useContext(ZPLContext);