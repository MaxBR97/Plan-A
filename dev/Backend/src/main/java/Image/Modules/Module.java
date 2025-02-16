package Image.Modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import Image.Image;
import Model.ModelParameter;
import Model.ModelSet;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorColumn(name = "module_type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "modules")
public abstract class Module {

    @EmbeddedId
    private ModuleId id;

    @Column(name = "description")
    private String description;

    @ElementCollection
    @CollectionTable(
        name = "input_sets",
        joinColumns = {
            @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable = false),
            @JoinColumn(name = "module_name", referencedColumnName = "name", nullable = false)
        }
    )
    @Column(name = "input_set")
    protected Set<String> inputSets = new HashSet<>();

    
    // @ElementCollection
    // @CollectionTable(name = "input_params", joinColumns= {@JoinColumn(name="image_id"),@JoinColumn(name = "module_name", referencedColumnName = "name")})
    // @Column(name = "input_param")
    @Transient
    protected Set<String> inputParams = new HashSet<>();

    protected Module() {
        // Required by JPA
        this.id = new ModuleId("0", "noname");
        this.description = "";
        inputSets = new HashSet<>();
        inputParams = new HashSet<>();
    }

    public Module(Image image, String name, String description) {
        this.id = new ModuleId(image.getId(), name);
        this.description = description;
     //   isActive=true;
        inputSets = new HashSet<>();
        inputParams = new HashSet<>();
    }

    public Module(Image image, String name, String description, Collection<String> inputSets, Collection<String> inputParams) {
        this.id = new ModuleId(image.getId(), name);
        this.description = description;
        //   isActive=true;
       this.inputSets = new HashSet<>(inputSets);
       this.inputParams = new HashSet<>(inputParams);
    }

    public String getId() {
        return this.id.getId();
    }

    public void setId(String id) {
        this.id.setId(id);
    }


    public String getName() {
        return this.id.getName();
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
