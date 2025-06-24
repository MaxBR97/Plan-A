# 3.3 Databases

## 3.3.1 Entity-Relationship Diagram (ERD)

The Plan-A application uses a relational database to persist mathematical optimization model configurations and their components. The database schema follows a modular design where each Image (model configuration) contains multiple modules, and each module contains various model components.

### Core Database Entities:

1. **images** - The central entity representing a mathematical optimization model configuration
2. **modules** - Abstract base for organizing model components (uses single table inheritance)
3. **Model Components** - Six separate tables for different types of model elements:
   - **sets** - Mathematical sets containing elements
   - **parameters** - Model parameters with values
   - **functions** - Mathematical functions
   - **constraints** - Optimization constraints
   - **preferences** - Objective function preferences
   - **variables** - Decision variables to be optimized
4. **Supporting Tables** - Configuration and metadata tables

### Key Relationships:

- **One-to-Many**: Each Image contains multiple Modules and Model Components
- **Many-to-One**: All components belong to a specific Image and optionally to a Module
- **Self-Reference**: Variables can be bound to Sets within the same Image
- **Specialized**: Preference modules have additional cost parameter associations

## 3.3.2 Main Tables and Structure

### Core Tables:

#### `images` Table
- **Purpose**: Stores model configuration metadata
- **Key Fields**:
  - `image_id` (varchar, PK): Unique identifier for the model
  - `image_name` (varchar): Human-readable name
  - `image_description` (varchar(500)): Model description
  - `owner` (varchar): User who owns the model
  - `is_private` (boolean): Privacy setting
  - `is_configured` (boolean): Configuration status
  - `is_popular` (boolean): Popularity flag

#### `modules` Table
- **Purpose**: Base table for module organization using single table inheritance
- **Key Fields**:
  - `image_id` + `name` (composite PK): Unique module identifier
  - `module_type` (varchar): Discriminator ('CONSTRAINT', 'PREFERENCE', 'VARIABLE')
  - `description` (varchar): Module description

#### Model Component Tables

##### `sets` Table
- **Purpose**: Stores mathematical sets used in the model
- **Key Fields**:
  - `image_id` + `name` (composite PK): Unique set identifier
  - `my_type` (text): JSON-serialized ModelType
  - `alias` (varchar): Human-readable name
  - `tags` (text): JSON-serialized string array for categorization
  - `module_name` (varchar): Associated module (optional)

##### `parameters` Table
- **Purpose**: Stores model parameters with their values
- **Key Fields**:
  - `image_id` + `name` (composite PK): Unique parameter identifier
  - `my_type` (text): JSON-serialized ModelType
  - `alias` (varchar): Human-readable name
  - `tags` (text): JSON-serialized string array
  - `is_cost_param` (boolean): Indicates if this is a cost parameter
  - `module_name` (varchar): Associated module (optional)

##### `variables` Table
- **Purpose**: Stores decision variables for optimization
- **Key Fields**:
  - `image_id` + `name` (composite PK): Unique variable identifier
  - `my_type` (text): JSON-serialized ModelType
  - `alias` (varchar): Human-readable name
  - `tags` (text): JSON-serialized string array
  - `is_complex` (boolean): Indicates complex variable
  - `is_binary` (boolean): Indicates binary variable (0 or 1)
  - `bound_set_image_id` + `bound_set_name`: Reference to bounding set

##### `constraints`, `preferences`, `functions` Tables
- **Purpose**: Store respective model components
- **Key Fields**: Similar structure to other component tables with type-specific fields

### Supporting Tables:

#### `preference_module_cost_params` Table
- **Purpose**: Associates cost parameters with preference modules
- **Key Fields**:
  - `image_id` + `module_name` + `param_identifier` (composite PK)
  - Links preference modules to their cost parameters

#### `image_solver_scripts` Table
- **Purpose**: Stores solver configuration scripts
- **Key Fields**:
  - `image_id` + `script_key` (composite PK)
  - `script_value` (text): Solver configuration

#### `image_saved_solutions` Table
- **Purpose**: Tracks saved solution names
- **Key Fields**:
  - `image_id` + `solution_order` (composite PK)
  - `solution_name` (varchar(255)): Name of saved solution

## 3.3.3 Main Transactions

### 1. Image Creation Transaction
**Purpose**: Create a new mathematical optimization model configuration
**Entities Modified**:
- `images`: Insert new image record
- `modules`: Insert default VariableModule
- `variables`: Insert initial variable components
**Process**:
1. Validate image name and owner
2. Insert image record with default settings
3. Create default VariableModule
4. Initialize basic variable structure

### 2. Image Retrieval Transactions

#### 2.1 Find Image by ID and Owner
**Purpose**: Retrieve a specific image owned by a user
**Entities Accessed**:
- `images`: Read image record
**Query**: `findByIdAndOwner(imageId, owner)`
**Process**:
1. Search images table by image_id and owner
2. Return complete image entity with all related data

#### 2.2 Find Accessible Image
**Purpose**: Retrieve an image that a user can access (owned or public)
**Entities Accessed**:
- `images`: Read image record
**Query**: `findByIdAndAccessible(imageId, owner)`
**Process**:
1. Search images table by image_id
2. Check if user is owner OR image is not private
3. Return image if accessible

#### 2.3 Search Images by Name
**Purpose**: Find images matching search criteria with access control
**Entities Accessed**:
- `images`: Read image records
**Query**: `searchShallowImages(searchPhrase, owner, pageable)`
**Process**:
1. Search images by name (case-insensitive LIKE)
2. Filter by access control (owner OR public)
3. Filter by configured status
4. Return paginated results with shallow projection

#### 2.4 Search Public Images
**Purpose**: Find public images matching search criteria
**Entities Accessed**:
- `images`: Read image records
**Query**: `searchShallowImages(searchPhrase, pageable)`
**Process**:
1. Search images by name (case-insensitive LIKE)
2. Filter by public access only
3. Filter by configured status
4. Return paginated results with shallow projection

#### 2.5 Find Images by Owner
**Purpose**: Retrieve all configured images owned by a user
**Entities Accessed**:
- `images`: Read image records
**Query**: `findByOwner(owner)`
**Process**:
1. Search images table by owner
2. Filter by configured status
3. Return list of shallow image projections

#### 2.6 Get Image Owner
**Purpose**: Retrieve the owner of a specific image
**Entities Accessed**:
- `images`: Read owner field
**Query**: `findOwner(imageId)`
**Process**:
1. Search images table by image_id
2. Return owner string

### 3. Image Deletion Transaction
**Purpose**: Delete an image and all related data
**Entities Modified**:
- `preference_module_cost_params`: Delete cost parameter associations
- `modules`: Delete all modules for the image
- `image_solver_scripts`: Delete solver configurations
- `image_saved_solutions`: Delete saved solution metadata
- `images`: Delete the image record
**Query**: `deleteImageAndRelatedData(imageId, owner)`
**Process**:
1. Validate ownership
2. Delete all related data in proper order (respecting foreign key constraints)
3. Delete the main image record
4. Ensure atomicity of the entire operation

### 4. Module Configuration Transaction
**Purpose**: Configure modules within an image
**Entities Modified**:
- `modules`: Insert/update module records
- `sets`, `parameters`, `functions`, `constraints`, `preferences`, `variables`: Insert/update component records
- `preference_module_cost_params`: Associate cost parameters with preference modules
**Process**:
1. Validate module configuration
2. Update module records
3. Insert/update associated components
4. Handle cost parameter associations for preference modules

### 5. Model Input Configuration Transaction
**Purpose**: Set input values for sets and parameters
**Entities Modified**:
- `sets`: Update set elements (stored as JSON in transient fields)
- `parameters`: Update parameter values (stored as JSON in transient fields)
**Process**:
1. Validate input compatibility with component types
2. Update component values
3. Trigger model recompilation if needed

### 6. Solver Configuration Transaction
**Purpose**: Configure solver settings for an image
**Entities Modified**:
- `image_solver_scripts`: Insert/update solver configuration
**Process**:
1. Validate solver script syntax
2. Store configuration scripts
3. Associate with specific image

### 7. Component Update Transactions

#### 7.1 Set Update Transaction
**Purpose**: Update set properties and metadata
**Entities Modified**:
- `sets`: Update alias, tags, my_type
**Process**:
1. Validate set exists and user has access
2. Update set properties
3. Maintain referential integrity

#### 7.2 Parameter Update Transaction
**Purpose**: Update parameter properties and metadata
**Entities Modified**:
- `parameters`: Update alias, tags, my_type, is_cost_param
**Process**:
1. Validate parameter exists and user has access
2. Update parameter properties
3. Update cost parameter associations if needed

#### 7.3 Variable Update Transaction
**Purpose**: Update variable properties and bindings
**Entities Modified**:
- `variables`: Update alias, tags, my_type, is_complex, is_binary, bound_set references
**Process**:
1. Validate variable exists and user has access
2. Update variable properties
3. Validate bound set references
4. Update foreign key relationships

### 8. Module-Specific Transactions

#### 8.1 Constraint Module Update Transaction
**Purpose**: Update constraint module configuration
**Entities Modified**:
- `modules`: Update module description
- `constraints`: Add/remove constraints from module
**Process**:
1. Validate module exists and user has access
2. Update module metadata
3. Manage constraint associations

#### 8.2 Preference Module Update Transaction
**Purpose**: Update preference module configuration
**Entities Modified**:
- `modules`: Update module description
- `preferences`: Add/remove preferences from module
- `preference_module_cost_params`: Update cost parameter associations
**Process**:
1. Validate module exists and user has access
2. Update module metadata
3. Manage preference associations
4. Update cost parameter mappings

#### 8.3 Variable Module Update Transaction
**Purpose**: Update variable module configuration
**Entities Modified**:
- `modules`: Update module description
- `variables`: Add/remove variables from module
**Process**:
1. Validate module exists and user has access
2. Update module metadata
3. Manage variable associations

### 9. Privacy and Access Control Transactions

#### 9.1 Update Image Privacy Transaction
**Purpose**: Change image privacy settings
**Entities Modified**:
- `images`: Update is_private field
**Process**:
1. Validate user owns the image
2. Update privacy setting
3. Maintain access control consistency

#### 9.2 Update Image Popularity Transaction
**Purpose**: Mark/unmark image as popular
**Entities Modified**:
- `images`: Update is_popular field
**Process**:
1. Validate user has appropriate permissions
2. Update popularity flag

### 10. Configuration Status Transactions

#### 10.1 Mark Image as Configured Transaction
**Purpose**: Mark image as fully configured
**Entities Modified**:
- `images`: Update is_configured field
**Process**:
1. Validate all required components are present
2. Update configuration status
3. Trigger validation checks

### 11. Batch Operations

#### 11.1 Bulk Image Retrieval Transaction
**Purpose**: Retrieve multiple images efficiently
**Entities Accessed**:
- `images`: Read multiple image records
**Process**:
1. Apply pagination and filtering
2. Return optimized result sets
3. Handle large result sets efficiently

#### 11.2 Bulk Component Update Transaction
**Purpose**: Update multiple components in a single transaction
**Entities Modified**:
- Multiple component tables based on operation
**Process**:
1. Validate all updates
2. Execute updates in batch
3. Maintain consistency across all affected entities

## 3.3.4 Data Object to Database Entity Mapping

The following data objects map directly to database entities:

### Direct Mappings:

1. **Image** → `images` table
   - All persistent fields map directly
   - Transient fields (model, solverScripts, savedSolutions) are loaded from related tables

2. **Module** → `modules` table
   - Uses single table inheritance with `module_type` discriminator
   - Transient fields (inputSets, inputParams) are loaded from component tables

3. **ModelSet** → `sets` table
   - Persistent fields: id, myType, alias, tags
   - Transient fields: values, def_values, myStruct, dependencies

4. **ModelParameter** → `parameters` table
   - Persistent fields: id, myType, alias, tags, isCostParameter
   - Transient fields: values, def_values, myStruct, dependencies

5. **ModelVariable** → `variables` table
   - Persistent fields: id, myType, alias, tags, isComplex, isBinary, boundSet
   - Transient fields: dependencies

6. **ModelConstraint** → `constraints` table
   - Persistent fields: id
   - Transient fields: dependencies

7. **ModelPreference** → `preferences` table
   - Persistent fields: id
   - Transient fields: dependencies

8. **ModelFunction** → `functions` table
   - Persistent fields: id, myType, alias, tags, isCostFunction
   - Transient fields: value, dependencies

### Specialized Mappings:

9. **PreferenceModule** → `modules` + `preference_module_cost_params` tables
   - Module metadata in `modules` table
   - Cost parameter associations in `preference_module_cost_params` table

10. **ConstraintModule** → `modules` table
    - All data stored in `modules` table with `module_type = 'CONSTRAINT'`

11. **VariableModule** → `modules` table
    - All data stored in `modules` table with `module_type = 'VARIABLE'`

### Non-Mapped Objects:

- **Model**: Not directly mapped - represents runtime model state
- **Solution**: Not directly mapped - represents solver output
- **ScipProcess**: Not directly mapped - represents runtime solver process
- **SolverService**: Not directly mapped - represents service layer
- **DTOs**: Not directly mapped - represent API data transfer

### Transient Data:

The following fields are transient and not persisted:
- All dependency lists (setDependencies, paramDependencies, functionDependencies)
- Runtime values (values, def_values in ModelInput classes)
- Structure information (myStruct)
- Runtime state (isRunning, processStatus in ScipProcess)
- Cached data (circularBuffer, solutionOutputStream)

This mapping ensures that the database stores only the essential configuration data while runtime state and computed values are managed in memory for performance. 