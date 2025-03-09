import { createContext, useContext, useState } from "react";

const ZPLContext = createContext(null);

const initialImageState = {
  imageName: "My Image",
  imageDescription: "default description",
  imageId: null,
  constraintModules: [],
  preferenceModules: [],
  // variables: [],
  // setTypes: [],
  // setTags: [],
  // paramTypes: [],
  // varTypes: [],
  variablesModule: null,
};

export const ZPLProvider = ({ children }) => {
  // State for image-related data
  const [image, setImage] = useState(initialImageState);

  // State for model-related data (from ModelDTO)
  const [model, setModel] = useState({
    constraints: new Set(),
    preferences: new Set(),
    variables: new Set(),
    setTypes: {},
    paramTypes: {},
    varTypes: {},
  });

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

  return (
    <ZPLContext.Provider
      value={{
        image,
        model,
        solutionResponse,
        updateImage,
        updateImageField,
        updateModel,
        updateSolutionResponse,
        initialImageState
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
