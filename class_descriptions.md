# 5.2 Class Descriptions

## 5.2.1 Main Classes

### Image Class
**Responsibilities:**
- Manages complete model configuration (Image)
- Coordinates modules and their components
- Handles image metadata and access control
- Manages solver configuration and solution processing

**Key Methods:**

#### Constructor Image(String id, String name, String description, String ownerUser, Boolean isPrivate)
- **Pre-condition:** id != null && name != null && ownerUser != null
- **Post-condition:** image is created with specified properties
- **Invariant:** image ID is unique and owner is valid
- **OCL:** self.id != null and self.owner != null

#### void update(ImageDTO imageDTO)
- **Pre-condition:** imageDTO != null && imageDTO is valid
- **Post-condition:** image properties are updated according to DTO
- **Invariant:** image ID and owner remain unchanged
- **Implementation hint:** Should validate DTO before applying changes

#### boolean isConfigured()
- **Pre-condition:** image is properly initialized
- **Post-condition:** returns true if image has all required components
- **Invariant:** configured status reflects actual component completeness
- **OCL:** self.isConfigured implies self.getVariablesModule() != null

#### void addConstraintModule(String moduleName, String description)
- **Pre-condition:** moduleName != null && !moduleName.isEmpty()
- **Post-condition:** new constraint module is added to image
- **Invariant:** module name is unique within image
- **OCL:** self.constraintsModules->select(m | m.getName() = moduleName)->isEmpty()

#### InputDTO getInput()
- **Pre-condition:** image has input components configured
- **Post-condition:** returns current input configuration
- **Invariant:** returned DTO contains all input components
- **OCL:** result.setsToValues != null and result.paramsToValues != null

### Model Class
**Responsibilities:**
- Manages the complete mathematical optimization model lifecycle
- Handles model source code parsing and compilation
- Manages model components (sets, parameters, functions, constraints, preferences, variables)
- Coordinates input/output operations and solver integration
- Maintains model state and configuration

**Key Methods:**

#### Constructor Model(ModelRepository repo, String id)
- **Pre-condition:** repo != null && id != null && !id.isEmpty()
- **Post-condition:** model is initialized with given repository and ID
- **Invariant:** this.id != null && this.modelRepository != null

#### void parseSource()
- **Pre-condition:** model source is available and accessible
- **Post-condition:** model components are parsed and loaded from source
- **Invariant:** source file exists and is readable
- **Implementation hint:** Should handle parsing exceptions gracefully

#### void setInput(ModelParameter identifier, String value)
- **Pre-condition:** identifier != null && value != null && parameter exists in model
- **Post-condition:** parameter value is updated and model is ready for recompilation
- **Invariant:** parameter type is compatible with provided value
- **OCL:** self.params->exists(p | p.getIdentifier() = identifier)

#### boolean isCompiling(float timeout)
- **Pre-condition:** timeout > 0
- **Post-condition:** returns true if model is currently compiling within timeout
- **Invariant:** compilation process is active and responsive
- **Implementation hint:** Should use timeout mechanism to prevent blocking

#### ModelSet getSet(String identifier)
- **Pre-condition:** identifier != null
- **Post-condition:** returns set with specified identifier or null if not found
- **Invariant:** returned set belongs to this model
- **OCL:** result != null implies result.getImageId() = self.id

### SolverService Class
**Responsibilities:**
- Provides high-level solver service interface
- Manages solver process pool and lifecycle
- Coordinates model solving and result processing

**Key Methods:**

#### Solution solve(String fileId, int timeout, String solverScript)
- **Pre-condition:** fileId != null && timeout > 0
- **Post-condition:** returns solution for specified model
- **Invariant:** solution is valid and complete
- **Implementation hint:** Should use process pool for concurrent solving

#### CompletableFuture<Solution> solveAsync(String fileId, int timeout, String solverScript)
- **Pre-condition:** fileId != null && timeout > 0
- **Post-condition:** returns future for asynchronous solving
- **Invariant:** future completes with valid solution or exception
- **Implementation hint:** Should handle cancellation and timeout properly

#### String isCompiling(String fileId, int timeout)
- **Pre-condition:** fileId != null && timeout > 0
- **Post-condition:** returns compilation status
- **Invariant:** status reflects actual compilation state
- **OCL:** result != null

#### void shutdown()
- **Pre-condition:** service is running
- **Post-condition:** all solver processes are terminated
- **Invariant:** no active processes remain after shutdown
- **Implementation hint:** Should be called during application shutdown

## 5.2.2 Core Model Classes

### ModelComponentId Class
**Responsibilities:**
- Provides unique identification for model components
- Encapsulates component identifier and image association
- Ensures proper equality and hash code implementation

**Key Methods:**

#### Constructor ModelComponentId(String identifier, String imageId)
- **Pre-condition:** identifier != null && imageId != null
- **Post-condition:** component ID is created with specified values
- **Invariant:** this.identifier != null && this.imageId != null

#### boolean equals(Object o)
- **Pre-condition:** o != null
- **Post-condition:** returns true if objects have same identifier and imageId
- **Invariant:** reflexive, symmetric, and transitive
- **Implementation hint:** Should be synchronized if used in concurrent contexts

### ModelComponent (Abstract) Class
**Responsibilities:**
- Base class for all model components
- Manages component dependencies and relationships
- Provides common functionality for component lifecycle

**Key Methods:**

#### void dynamicLoadTransient(ModelComponent mc)
- **Pre-condition:** mc != null && mc has same identifier
- **Post-condition:** transient fields are loaded from provided component
- **Invariant:** component identifier remains unchanged
- **Implementation hint:** Should handle null dependencies gracefully

#### boolean isPrimitive()
- **Pre-condition:** component is properly initialized
- **Post-condition:** returns true if component has no dependencies
- **Invariant:** primitive components have empty dependency lists
- **OCL:** self.setDependencies->isEmpty() and self.paramDependencies->isEmpty() and self.functionDependencies->isEmpty()

## 5.2.3 Model Input/Output Classes

### ModelInput (Abstract) Class
**Responsibilities:**
- Base class for all input model components (sets, parameters, functions)
- Manages type compatibility and validation
- Handles value storage and retrieval

**Key Methods:**

#### boolean isCompatible(ModelType val)
- **Pre-condition:** val != null && myType != null
- **Post-condition:** returns true if types are compatible
- **Invariant:** type compatibility is transitive
- **Implementation hint:** Should cache compatibility results for performance

#### void setAlias(String alias)
- **Pre-condition:** alias != null
- **Post-condition:** component alias is updated
- **Invariant:** alias is human-readable and meaningful
- **OCL:** alias.size() > 0 and alias.size() <= 255

### ModelOutput (Abstract) Class
**Responsibilities:**
- Base class for output model components (variables)
- Manages output type information and structure
- Handles complex output types

**Key Methods:**

#### boolean isComplex()
- **Pre-condition:** component is properly initialized
- **Post-condition:** returns true if component has complex structure
- **Invariant:** complex components have non-null structure
- **OCL:** self.isComplex implies self.getStructure() != null

## 5.2.4 Concrete Model Components

### ModelSet Class
**Responsibilities:**
- Represents mathematical sets in optimization models
- Manages set elements and operations
- Handles set type validation and structure

**Key Methods:**

#### void update(SetDefinitionDTO dto)
- **Pre-condition:** dto != null && dto is valid
- **Post-condition:** set properties are updated according to DTO
- **Invariant:** set identifier remains unchanged
- **Implementation hint:** Should validate DTO before applying changes

#### List<String> getElements()
- **Pre-condition:** set is properly initialized
- **Post-condition:** returns current set elements
- **Invariant:** returned list is not null (may be empty)
- **OCL:** result != null

#### void addElement(String element)
- **Pre-condition:** element != null && element is valid for set type
- **Post-condition:** element is added to set if not already present
- **Invariant:** set maintains uniqueness of elements
- **OCL:** self.getElements()->includes(element) implies self.getElements()->count(element) = 1

### ModelParameter Class
**Responsibilities:**
- Represents model parameters with values
- Manages parameter types and cost parameter designation
- Handles parameter value validation and storage

**Key Methods:**

#### void update(ParameterDefinitionDTO dto)
- **Pre-condition:** dto != null && dto is valid
- **Post-condition:** parameter properties are updated according to DTO
- **Invariant:** parameter identifier remains unchanged
- **Implementation hint:** Should validate parameter type compatibility

#### String getValue()
- **Pre-condition:** parameter has been set with a value
- **Post-condition:** returns current parameter value
- **Invariant:** returned value is compatible with parameter type
- **OCL:** self.hasValue() implies result != null

#### boolean isCostParameter()
- **Pre-condition:** parameter is properly initialized
- **Post-condition:** returns true if parameter is designated as cost parameter
- **Invariant:** cost parameter designation is consistent
- **Implementation hint:** Should be used in preference module cost calculations

### ModelVariable Class
**Responsibilities:**
- Represents decision variables in optimization models
- Manages variable types (binary, complex) and bound sets
- Handles variable constraints and relationships

**Key Methods:**

#### void update(VariableDTO dto)
- **Pre-condition:** dto != null && dto is valid
- **Post-condition:** variable properties are updated according to DTO
- **Invariant:** variable identifier remains unchanged
- **Implementation hint:** Should validate bound set references

#### void setBoundSet(ModelSet s)
- **Pre-condition:** s != null && s belongs to same image
- **Post-condition:** variable is bound to specified set
- **Invariant:** bound set is compatible with variable type
- **OCL:** s != null implies s.getImageId() = self.getImageId()

#### boolean isBinary()
- **Pre-condition:** variable is properly initialized
- **Post-condition:** returns true if variable is binary (0 or 1)
- **Invariant:** binary variables have appropriate type constraints
- **Implementation hint:** Should be used in solver configuration

## 5.2.5 Image and Module Classes

### Module (Abstract) Class
**Responsibilities:**
- Base class for all module types
- Manages module metadata and input components
- Provides common module functionality

**Key Methods:**

#### Module(Image image, String name, String description)
- **Pre-condition:** image != null && name != null
- **Post-condition:** module is created with specified properties
- **Invariant:** module belongs to specified image
- **OCL:** self.image = image

#### Set<ModelSet> getInvolvedSets()
- **Pre-condition:** module is properly initialized
- **Post-condition:** returns all sets involved in module
- **Invariant:** returned set includes input sets and dependency sets
- **OCL:** self.inputSets->forAll(s | result->includes(s))

#### boolean isInput(String modelInputId)
- **Pre-condition:** modelInputId != null
- **Post-condition:** returns true if component is input to module
- **Invariant:** input components are properly tracked
- **OCL:** result implies (self.inputSets->exists(s | s.getIdentifier() = modelInputId) or self.inputParams->exists(p | p.getIdentifier() = modelInputId))

### ConstraintModule Class
**Responsibilities:**
- Manages optimization constraints within a module
- Coordinates constraint components and their relationships
- Handles constraint module configuration

**Key Methods:**

#### void addConstraint(ModelConstraint constraint)
- **Pre-condition:** constraint != null && constraint belongs to same image
- **Post-condition:** constraint is added to module
- **Invariant:** constraint identifier is unique within module
- **OCL:** self.constraints->select(c | c.getIdentifier() = constraint.getIdentifier())->isEmpty()

#### void removeConstraint(String identifier)
- **Pre-condition:** identifier != null && constraint exists in module
- **Post-condition:** constraint is removed from module
- **Invariant:** module remains in valid state
- **OCL:** self.constraints->select(c | c.getIdentifier() = identifier)->isEmpty()

### PreferenceModule Class
**Responsibilities:**
- Manages objective function preferences within a module
- Coordinates preference components and cost parameters
- Handles preference module configuration

**Key Methods:**

#### void addPreference(ModelPreference preference)
- **Pre-condition:** preference != null && preference belongs to same image
- **Post-condition:** preference is added to module
- **Invariant:** preference identifier is unique within module
- **OCL:** self.preferences->select(p | p.getIdentifier() = preference.getIdentifier())->isEmpty()

#### Set<ModelParameter> getCostParameters()
- **Pre-condition:** module is properly initialized
- **Post-condition:** returns all cost parameters associated with module
- **Invariant:** returned parameters are valid cost parameters
- **OCL:** result->forAll(p | p.isCostParameter())

### VariableModule Class
**Responsibilities:**
- Manages decision variables within a module
- Coordinates variable components and their relationships
- Handles variable module configuration

**Key Methods:**

#### void addVariable(ModelVariable variable)
- **Pre-condition:** variable != null && variable belongs to same image
- **Post-condition:** variable is added to module
- **Invariant:** variable identifier is unique within module
- **OCL:** self.variables->select(v | v.getIdentifier() = variable.getIdentifier())->isEmpty()

#### Set<String> getIdentifiers()
- **Pre-condition:** module is properly initialized
- **Post-condition:** returns all variable identifiers in module
- **Invariant:** returned set contains valid identifiers
- **OCL:** result->forAll(id | id != null and id.size() > 0)

## 5.2.6 Repository Classes

### ImageRepository Interface
**Responsibilities:**
- Provides data access for Image entities
- Manages image persistence and retrieval
- Handles image access control and search

**Key Methods:**

#### Image findByIdAndOwner(String imageId, String owner)
- **Pre-condition:** imageId != null && owner != null
- **Post-condition:** returns image if owned by specified user
- **Invariant:** returned image belongs to specified owner
- **OCL:** result != null implies result.getOwner() = owner

#### List<ShallowImageProjection> searchShallowImages(String searchPhrase, String owner, Pageable pageable)
- **Pre-condition:** searchPhrase != null && owner != null && pageable != null
- **Post-condition:** returns paginated search results
- **Invariant:** results respect access control
- **OCL:** result->forAll(img | img.getOwner() = owner or not img.isPrivate())

#### void deleteImageAndRelatedData(String imageId, String owner)
- **Pre-condition:** imageId != null && owner != null
- **Post-condition:** image and all related data are deleted
- **Invariant:** deletion is atomic and complete
- **Implementation hint:** Should use transaction to ensure consistency

### ModelRepository Interface
**Responsibilities:**
- Provides data access for model files
- Manages model file storage and retrieval
- Handles file system operations

**Key Methods:**

#### InputStream downloadDocument(String id)
- **Pre-condition:** id != null && document exists
- **Post-condition:** returns input stream for document
- **Invariant:** stream is readable and contains document data
- **OCL:** result != null

#### void uploadDocument(String id, InputStream inputStream)
- **Pre-condition:** id != null && inputStream != null
- **Post-condition:** document is stored with specified ID
- **Invariant:** document is accessible after upload
- **OCL:** self.downloadDocument(id) != null

## 5.2.7 Solution and Types

### Solution Class
**Responsibilities:**
- Represents optimization solution results
- Manages solution parsing and data extraction
- Provides solution status and metrics

**Key Methods:**

#### Solution parseSolutionStatus()
- **Pre-condition:** solution file exists and is readable
- **Post-condition:** solution status is parsed and set
- **Invariant:** status reflects actual solution state
- **OCL:** self.solved != null

#### void parseSolution(ModelInterface model, Set<String> varsToParse)
- **Pre-condition:** model != null && varsToParse != null
- **Post-condition:** solution data is parsed for specified variables
- **Invariant:** parsed data is consistent with model structure
- **OCL:** self.variableSolution != null

#### double getObjectiveValue()
- **Pre-condition:** solution has been parsed
- **Post-condition:** returns objective function value
- **Invariant:** value is finite and meaningful
- **OCL:** result.isFinite()

### ModelType Class
**Responsibilities:**
- Represents model component types
- Manages type compatibility and validation
- Provides type information for components

**Key Methods:**

#### boolean isCompatible(ModelType val)
- **Pre-condition:** val != null
- **Post-condition:** returns true if types are compatible
- **Invariant:** compatibility is reflexive and symmetric
- **OCL:** self = val implies result = true

## 5.2.8 DTO Classes

### ModelDTO Class
**Responsibilities:**
- Data transfer object for complete model structure
- Encapsulates model components for API communication
- Provides model metadata and type information

**Invariants:**
- All component collections are not null (may be empty)
- Type maps are consistent with component collections
- DTO represents valid model structure

### InputDTO Class
**Responsibilities:**
- Data transfer object for model input configuration
- Encapsulates input values and module toggles
- Provides input validation and structure

**Invariants:**
- Input maps are not null (may be empty)
- Toggle lists contain valid module names
- Input values are compatible with component types

## 5.2.9 Enums

### SolutionStatus Enum
**Responsibilities:**
- Represents optimization solution status
- Provides status constants for solution evaluation
- Ensures consistent status representation

**Values:**
- OPTIMAL: Solution is optimal
- SUBOPTIMAL: Solution is suboptimal but feasible
- UNSOLVED: Problem could not be solved

**Invariants:**
- Status values are mutually exclusive
- Status accurately reflects solution quality
- Status is immutable once set 