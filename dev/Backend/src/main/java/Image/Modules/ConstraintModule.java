package Image.Modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

import DTO.Records.Image.ConstraintModuleDTO;
import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import Image.Image;
import Model.ModelConstraint;
import Model.ModelParameter;
import Model.ModelSet;
import Model.ModelVariable;
import ch.qos.logback.core.model.Model;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

@Entity
@DiscriminatorValue("CONSTRAINT")
public class ConstraintModule extends Module{
    /**
     * a constraint module, holding the user definition for a group of model constraints
     * (a group of subTo expressions in zimpl code)
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable = false),
        @JoinColumn(name = "module_name", referencedColumnName = "name", nullable = false)
    })
    @MapKey(name = "id.identifier")
    //@Transient
    private Map<String,ModelConstraint> constraints;


    public ConstraintModule(Image image, ConstraintModuleDTO dto) throws Exception {
        super(image, dto.moduleName(), dto.description(), dto.inputSets(), dto.inputParams());
        this.constraints = new HashMap<>();
        for (String consDTO : dto.constraints()) {
            ModelConstraint cons = image.getModel().getConstraint(consDTO);
            this.constraints.put(consDTO, cons);
            cons.setModuleName(this.getName());
        }
    }

    public ConstraintModule(Image image , String name, String description) {
        super(image, name, description);
        this.constraints = new HashMap<>();
    }
    public ConstraintModule(Image image ,String name, String description, Collection<ModelConstraint> constraints, Collection<ModelSet> inputSets, Collection<ModelParameter> inputParams) {
        super(image, name, description, inputSets, inputParams);
        this.constraints = new HashMap<>();
        for (ModelConstraint constraint : constraints) {
            this.constraints.put(constraint.getIdentifier(), constraint);
        }
    }

    public ConstraintModule() {
       super();
       this.constraints = new HashMap<String,ModelConstraint>();
    }
    /**
     * Fetch all ModelSets that are in use in any of the constraints in the module.
     * @return all sets that are part of any constraint in the module
     */
    @Override
    public Set<ModelSet> getInvolvedSets(){
        HashSet<ModelSet> involvedSets = new HashSet<>();
        for(ModelConstraint constraint : constraints.values()){
            constraint = ((ModelConstraint)loadFullComponent(constraint));
            constraint.getPrimitiveSets(involvedSets);
        }
        return involvedSets;
    }

    @Override
    public Set<ModelParameter> getInvolvedParameters() {
        HashSet<ModelParameter> involvedParameters = new HashSet<>();
        for(ModelConstraint preference : constraints.values()){
            preference = ((ModelConstraint)loadFullComponent(preference));
            preference.getPrimitiveParameters(involvedParameters);
        }
        return involvedParameters;
    }
    @Transactional
    public Map<String, ModelConstraint> getConstraints() {
        return constraints;
    }
    @Transactional
    public ModelConstraint getConstraint(String constraintName) {
        return constraints.get(constraintName);
    }
    @Transactional
    public void addConstraint(ModelConstraint constraint){
        constraints.put(constraint.getIdentifier(),constraint);
    }

    @Transactional
    public void removeConstraint(ModelConstraint constraint){
        constraints.remove(constraint.getIdentifier());
    }
    @Transactional
    public void removeConstraint(String identifier){
        constraints.remove(identifier);
    }

    @Transactional
    public void update(ConstraintModuleDTO dto) throws Exception {
        // Update constraints
        for (String constraintId : dto.constraints()) {
            ModelConstraint modelConstraint = image.getModel().getConstraint(constraintId);
            if (modelConstraint == null) {
                throw new IllegalArgumentException("Invalid constraint name: " + constraintId);
            }
            modelConstraint.setModuleName(this.getName());
            constraints.put(constraintId, modelConstraint);
        }

        // Update input sets
        inputSets.clear();
        for (SetDefinitionDTO setDTO : dto.inputSets()) {
            addSet(setDTO);
        }

        // Update input parameters
        inputParams.clear();
        for (ParameterDefinitionDTO paramDTO : dto.inputParams()) {
            addParam(paramDTO);
        }
    }
}
