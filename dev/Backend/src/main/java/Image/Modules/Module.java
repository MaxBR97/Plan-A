package Image.Modules;

import Model.ModelFunctionality;
import Model.ModelParameter;
import Model.ModelSet;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import Image.Image;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Use joined table inheritance
@Table(name = "modules")
public abstract class Module {

    @EmbeddedId
    private ModuleId id;

    @Column
    private String description;

    @ElementCollection
    @Column(name = "input_set")
    protected Set<String> inputSets = new HashSet<>();

    @ElementCollection
    @Column(name = "input_param")
    protected Set<String> inputParams = new HashSet<>();

    protected Module() {
        // Required by JPA
        inputSets = new HashSet<>();
        inputParams = new HashSet<>();
    }

    public Module(Image image, String name, String description) {
        id = new ModuleId(image.getId(), name);
        this.description = description;
     //   isActive=true;
        inputSets = new HashSet<>();
        inputParams = new HashSet<>();
    }
    public Module(Image image, String name, String description, Collection<String> inputSets, Collection<String> inputParams) {
        id = new ModuleId(image.getId(), name);
        this.description = description;
        //   isActive=true;
       this.inputSets = new HashSet<>(inputSets);
       this.inputParams = new HashSet<>(inputParams);
    }

    public ModuleId getId() {
        return id;
    }

    public void setId(ModuleId id) {
        this.id = id;
    }

    public String getImageId() {
        return id.getImageId();
    }

    public String getName() {
        return id.getName();
    }

   // private boolean isActive;

    /**
     * Changed to be part of ModuleDTO, no longer image's responsibility to get
     */
    @Deprecated
    public abstract Set<ModelSet> getInvolvedSets();
    @Deprecated
    public abstract Set<ModelParameter> getInvolvedParameters();
   
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Disables if its on and enables if it's off.
     */
   /* public void ToggleModule(){
        isActive=!isActive;
    }
    public boolean isActive(){
        return isActive;
    }*/
    public Set<String> getInputSets() {
        return inputSets;
    }

    public Set<String> getInputParams() {
        return inputParams;
    }
}
