package Image.Modules;

import Model.*;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.transaction.Transactional;

import java.util.*;

import Image.Image;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.Table;

// @Entity
// @Table(name = "constraint_module")
public class ConstraintModule extends Module{
    /**
     * a constraint module, holding the user definition for a group of model constraints
     * (a group of subTo expressions in zimpl code)
     */

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private String id;


    // @ElementCollection
    // @Column(name = "constraints")
    // @MapKey(name = "name")
    private final Map<String,ModelConstraint> constraints;

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
    public Map<String, ModelConstraint> getConstraints() {
        return constraints;
    }
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
