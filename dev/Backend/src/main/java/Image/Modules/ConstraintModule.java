package Image.Modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Image.Image;
import Model.ModelConstraint;
import Model.ModelParameter;
import Model.ModelSet;
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

    public ConstraintModule(Image image , String name, String description) {
        super(image, name, description);
        this.constraints = new HashMap<>();
    }
    public ConstraintModule(Image image ,String name, String description, Collection<ModelConstraint> constraints, Collection<String> inputSets, Collection<String> inputParams) {
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
            constraint.getPrimitiveSets(involvedSets);
        }
        return involvedSets;
    }

    @Override
    public Set<ModelParameter> getInvolvedParameters() {
        HashSet<ModelParameter> involvedParameters = new HashSet<>();
        for(ModelConstraint preference : constraints.values()){
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
}
