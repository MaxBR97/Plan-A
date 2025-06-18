import { createContext, useContext, useState, useEffect } from "react";
import axios from 'axios';

const ZPLContext = createContext(null);

const initialUser = {
  username: "Guest",
  isLoggedIn: false
}

const initialImageState = {
  imageName: "My Image",
  imageDescription: "",
  imageId: null,
  owner: initialUser.username,
  isPrivate: true,
  solverSettings: {"Default": "", Optimallity: "set emphasis optimality", "Tree search": "set emphasis tree search", Feasibility: "set emphasis feasibility", "Aggressive static analysis": "set presolving emphasis aggressive", "Numerics": "set emphasis numerics"},
  constraintModules: [],
  preferenceModules: [],
  variablesModule: {"variablesOfInterest": [], "inputSets": [], "inputParams": []},
  isConfigured: false,
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
  const [error, setError] = useState(null);
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

  const fetchAndSetImage = async (imageId) => {
    try {
      const response = await axios.get(`/api/images/${imageId ? imageId : image.imageId}`);
      updateImage(response.data);
      console.log("Fetched image: ", response.data);
      setError(null); // Clear any previous errors on success
    } catch (error) {
      console.error("Error fetching image:", error);
      setError(`Failed to fetch image: ${error.message}`);
    }
  };

  const deleteImage = async () => {
    try {
      const response = await axios.delete(`/api/images/${image.imageId}`);
      console.log("Deleted image: ", response);
    } catch (error) {
      console.error("Error deleting image:", error);
    }
  };

  // Patch image: set given image, but fill undefined/null fields with initialImageState values
  const patchImage = (partialImage) => {
    setImage(prev => {
      const patched = { ...initialImageState, ...prev, ...partialImage };
      // For each key in initialImageState, if patched[key] is null or undefined, use initialImageState[key]
      Object.keys(initialImageState).forEach(key => {
        if (patched[key] === undefined || patched[key] === null) {
          patched[key] = initialImageState[key];
        }
      });
      return patched;
    });
  };

  return (
    <ZPLContext.Provider
      value={{
        user,
        image,
        model,
        error,
        solutionResponse,
        updateUserField,
        updateImage,
        updateImageField,
        updateImageFieldWithCallBack,
        updateModel,
        updateSolutionResponse,
        resetImage,
        resetModel,
        resetSolutionResponse,
        fetchAndSetImage,
        deleteImage,
        patchImage
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
