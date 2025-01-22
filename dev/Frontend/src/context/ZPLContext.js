import React, { createContext, useState, useContext } from 'react';

// Create Context
const ZPLContext = createContext();

// Create a Provider Component
export const ZPLProvider = ({ children }) => {
    const [imageId, setImageId] = useState(null);
    const [variables, setVariables] = useState({});
    const [types, setTypes] = useState({});
    const [constraints, setConstraints] = useState({});
    const [preferences, setPreferences] = useState({});

    return (
        <ZPLContext.Provider value={{ 
            imageId, setImageId, 
            variables, setVariables, 
            types, setTypes,
            constraints, setConstraints,
            preferences, setPreferences
        }}>
            {children}
        </ZPLContext.Provider>
    );
};

// Custom Hook to Use the Context
export const useZPL = () => {
    return useContext(ZPLContext);
};
