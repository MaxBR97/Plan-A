import React, { createContext, useContext, useState } from "react";

const ZPLContext = createContext();

export const ZPLProvider = ({ children }) => {
    const [constraints, setConstraints] = useState([]);
    const [preferences, setPreferences] = useState([]);
    const [modules, setModules] = useState([]);
    const [preferenceModules, setPreferenceModules] = useState([]);
    const [variables, setVariables] = useState([]);
    const [types, setTypes] = useState({});
    const [imageId, setImageId] = useState(null);
    const [solutionResponse, setSolutionResponse] = useState(null); // <-- Store response here

    return (
        <ZPLContext.Provider value={{
            constraints, setConstraints,
            preferences, setPreferences,
            modules, setModules,
            preferenceModules, setPreferenceModules,
            variables, setVariables,
            types, setTypes,
            imageId, setImageId,
            solutionResponse, setSolutionResponse // Provide to context
        }}>
            {children}
        </ZPLContext.Provider>
    );
};

export const useZPL = () => useContext(ZPLContext);
