import React, { createContext, useContext, useState } from "react";

const ZPLContext = createContext();

export const ZPLProvider = ({ children }) => {
    const [constraints, setConstraints] = useState([]);
    const [preferences, setPreferences] = useState([]);
    const [modules, setModules] = useState([]);
    const [preferenceModules, setPreferenceModules] = useState([]);
    const [variables, setVariables] = useState([]);
    const [setTypes, setSetTypes] = useState({}); // ✅ NEW: Store set types
    const [paramTypes, setParamTypes] = useState({}); // ✅ NEW: Store param types
    const [varTypes, setVarTypes] = useState({}); // ✅ NEW: Store variable types
    const [imageId, setImageId] = useState(null);
    const [solutionResponse, setSolutionResponse] = useState(null); // Store response
    const [solutionData, setSolutionData] = useState(null);

    return (
        <ZPLContext.Provider value={{
            constraints, setConstraints,
            preferences, setPreferences,
            modules, setModules,
            preferenceModules, setPreferenceModules,
            variables, setVariables,
            setTypes, setSetTypes, // ✅ Provide setTypes
            paramTypes, setParamTypes, // ✅ Provide paramTypes
            varTypes, setVarTypes, // ✅ Provide varTypes
            imageId, setImageId,
            solutionResponse, setSolutionResponse,
            solutionData, setSolutionData
        }}>
            {children}
        </ZPLContext.Provider>
    );
};

export const useZPL = () => useContext(ZPLContext);
