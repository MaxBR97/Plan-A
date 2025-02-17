package Image.Modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import Image.Image;
import Model.ModelComponent;
import Model.ModelConstraint;
import Model.ModelParameter;
import Model.ModelSet;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    /*
     *  inputSets - sets chosen to be modifiable by the user - to recieve inputs. 
     *  These are not necessarily all input dependencies of the module.
     */
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

    
    /*
     *  inputParams - sets chosen to be modifiable by the user - to recieve inputs. 
     *  These are not necessarily all input dependencies of the module.
     */
    @ElementCollection
    @CollectionTable(
        name = "input_params",
        joinColumns = {
            @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable = false),
            @JoinColumn(name = "module_name", referencedColumnName = "name", nullable = false)
        }
    )
    @Column(name = "input_param")
    protected Set<String> inputParams = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "image_id", referencedColumnName = "image_id", insertable=false, updatable=false)
    private Image image;

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
        this.image = image;
     //   isActive=true;
        this.inputSets = new HashSet<>();
        this.inputParams = new HashSet<>();
    }

    public Module(Image image, String name, String description, Collection<String> inputSets, Collection<String> inputParams) {
        this.id = new ModuleId(image.getId(), name);
        this.description = description;
        this.image = image;
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
   
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getInputSets() {
        return inputSets;
    }

    public Set<String> getInputParams() {
        return inputParams;
    }

    public ModelComponent loadFullComponent(ModelComponent mc){
        return image.getModel().getComponent(mc.getIdentifier());
    }
    
    public abstract Set<ModelSet> getInvolvedSets();

    public abstract Set<ModelParameter> getInvolvedParameters();

}
