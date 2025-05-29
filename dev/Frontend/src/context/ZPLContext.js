import { createContext, useContext, useState, useEffect } from "react";

const ZPLContext = createContext(null);

const initialUser = {
  username: "guest",
  isLoggedIn: false
}

const initialImageState = {
  imageName: "My Image",
  imageDescription: "",
  imageId: null,
  owner: "guest",
  isPrivate: true,
  solverSettings: {"default": ""},
  constraintModules: [],
  preferenceModules: [],
  variablesModule: null,
};

const initialModelState = {
  constraints: new Set(),
  preferences: new Set(),
  variables: new Set(),
  setTypes: {},
  paramTypes: {},
  varTypes: {},
}

// Helper function to serialize Sets for localStorage
const serializeModel = (model) => {
  return {
    ...model,
    constraints: Array.from(model.constraints || []),
    preferences: Array.from(model.preferences || []),
    variables: Array.from(model.variables || [])
  };
};

// Helper function to deserialize Sets from localStorage
const deserializeModel = (modelData) => {
  if (!modelData) return initialModelState;
  return {
    ...modelData,
    constraints: new Set(modelData.constraints || []),
    preferences: new Set(modelData.preferences || []),
    variables: new Set(modelData.variables || [])
  };
};

export const ZPLProvider = ({ children, initialState = {} }) => {
  const [user, setUser] = useState(() => initialState.user || initialUser);

  // State for image-related data
  const [image, setImage] = useState(() => {
    // First try to get from initialState
    if (initialState.image) {
      return initialState.image;
    }
    // Then try localStorage
    const savedImage = localStorage.getItem('zpl_image');
    if (savedImage) {
      try {
        return JSON.parse(savedImage);
      } catch (e) {
        console.error('Failed to parse saved image:', e);
        return initialImageState;
      }
    }
    return initialImageState;
  });

  // State for model-related data (from ModelDTO)
  const [model, setModel] = useState(() => {
    // First try to get from initialState
    if (initialState.model) {
      return deserializeModel(initialState.model);
    }
    // Then try localStorage
    const savedModel = localStorage.getItem('zpl_model');
    if (savedModel) {
      try {
        return deserializeModel(JSON.parse(savedModel));
      } catch (e) {
        console.error('Failed to parse saved model:', e);
        return initialModelState;
      }
    }
    return initialModelState;
  });

  // State for solution response (from SolutionDTO)
  const [solutionResponse, setSolutionResponse] = useState(() => 
    initialState.solutionResponse || {
      solved: false,
      solvingTime: -2,
      objectiveValue: 0,
      errorMsg: "",
      solution: {},
    }
  );

  // Persist image changes to localStorage
  useEffect(() => {
    localStorage.setItem('zpl_image', JSON.stringify(image));
  }, [image]);

  // Persist model changes to localStorage
  useEffect(() => {
    localStorage.setItem('zpl_model', JSON.stringify(serializeModel(model)));
  }, [model]);

  // Function to update user fields dynamically
  const updateUserField = (field, value) => {  
    setUser((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const updateImage = (imageDTO) => {
     setImage(imageDTO);
  }

  // Function to update image fields dynamically
  const updateImageField = (field, value) => {  
    setImage((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const updateImageFieldWithCallBack = (field, value, callback) => {  
    setImage((prev) => ({
      ...prev,
      [field]: value,
    }), callback);
  };

  // Function to update model state
  const updateModel = (newModelData) => {
    setModel(deserializeModel(newModelData));
  };

  // Function to update solution response
  const updateSolutionResponse = (response) => {
    setSolutionResponse(response);
  };

  // Function to reset image state to initial values
  const resetImage = () => {
    setImage(initialImageState);
    localStorage.removeItem('zpl_image');
  };

  // Function to reset model state to initial values
  const resetModel = () => {
    setModel(initialModelState);
    localStorage.removeItem('zpl_model');
  };

  // Function to reset solution response to initial values
  const resetSolutionResponse = () => {
    setSolutionResponse({
      solved: false,
      solvingTime: -2,
      objectiveValue: 0,
      errorMsg: "",
      solution: {},
    });
  };

  return (
    <ZPLContext.Provider
      value={{
        user,
        image,
        model,
        solutionResponse,
        updateUserField,
        updateImage,
        updateImageField,
        updateImageFieldWithCallBack,
        updateModel,
        updateSolutionResponse,
        resetImage,
        resetModel,
        resetSolutionResponse
      }}
    >
      {children}
    </ZPLContext.Provider>
  );
};

export const useZPL = () => {
  const context = useContext(ZPLContext);
  if (!context) {
    throw new Error("useZPL must be used within a ZPLProvider");
  }
  return context;
};
