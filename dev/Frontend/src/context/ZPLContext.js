import { createContext, useContext, useState } from "react";

const ZPLContext = createContext(null);

const initialUser = {
  username: "guest",
  isLoggedIn: false
}

const initialImageState = {
  imageName: "My Image",
  imageDescription: "default description",
  imageId: null,
  owner: "guest",
  isPrivate: true,
  solverSettings: {"default": ""},
  constraintModules: [],
  preferenceModules: [],
  // variables: [],
  // setTypes: [],
  // setTags: [],
  // paramTypes: [],
  // varTypes: [],
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

export const ZPLProvider = ({ children }) => {
  const [user, setUser] = useState(initialUser);

  // State for image-related data
  const [image, setImage] = useState(initialImageState);

  // State for model-related data (from ModelDTO)
  const [model, setModel] = useState(initialModelState);

  // Function to update user fields dynamically
  const updateUserField = (field, value) => {  
    setUser((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  // State for solution response (from SolutionDTO)
  const [solutionResponse, setSolutionResponse] = useState({
    solved: false,
    solvingTime: -2,
    objectiveValue: 0,
    errorMsg: "",
    solution: {},
  });

  const updateImage = (imageDTO) => {
     setImage(imageDTO)
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
    setModel({
      constraints: new Set(newModelData.constraints || []),
      preferences: new Set(newModelData.preferences || []),
      variables: new Set(newModelData.variables || []),
      setTypes: newModelData.setTypes || {},
      paramTypes: newModelData.paramTypes || {},
      varTypes: newModelData.varTypes || {},
    });
  };

  // Function to update solution response
  const updateSolutionResponse = (response) => {
    setSolutionResponse(response);
  };

  const resetImage = () => {
    setImage(initialImageState)
  }

  const resetModel = () => {
    setModel(initialModelState)
  }

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
        resetModel
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
