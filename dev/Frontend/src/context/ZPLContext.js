import React, { createContext, useContext, useState } from "react";

const ZPLContext = createContext();

export const ZPLProvider = ({ children }) => {
    const [imageId, setImageId] = useState(null);
    const [variables, setVariables] = useState({});
    const [constraints, setConstraints] = useState({});
    const [preferences, setPreferences] = useState({});
    const [types, setTypes] = useState({});
    const [modules, setModules] = useState([]);
    const [preferenceModules, setPreferenceModules] = useState([]);


    return (
        <ZPLContext.Provider value={{ 
            imageId, setImageId, 
            variables, setVariables, 
            constraints, setConstraints, 
            preferences, setPreferences, 
            types, setTypes,
            modules, setModules,
            preferenceModules, setPreferenceModules
        }}>
            {children}
        </ZPLContext.Provider>
    );
};

export const useZPL = () => useContext(ZPLContext);
