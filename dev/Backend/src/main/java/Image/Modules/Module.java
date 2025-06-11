package Image.Modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import Image.Image;
import Model.ModelComponent;
import Model.ModelComponentId;
import Model.ModelParameter;
import Model.ModelSet;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.AttributeOverride;
import jakarta.transaction.Transactional;

@Entity
@DiscriminatorColumn(name = "module_type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "modules")
public abstract class Module {

    @EmbeddedId
    private ModuleId id;

    @Column(name = "description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "module_name", referencedColumnName = "name", nullable=false),
        @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable=false)
    })
    //@Transient
    protected Set<ModelSet> inputSets = new HashSet<>();

    
    /*
     *  inputParams - sets chosen to be modifiable by the user - to recieve inputs. 
     *  These are not necessarily all input dependencies of the module.
     */
    //@OneToMany(mappedBy = "module_name", cascade = CascadeType.ALL, orphanRemoval = true) // Assuming Book entity has 'author' field
    // @Column(name = "input_param")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "module_name", referencedColumnName = "name", nullable=false),
        @JoinColumn(name = "image_id", referencedColumnName = "image_id", nullable=false)
    })
    //@Transient
    protected Set<ModelParameter> inputParams = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "image_id", referencedColumnName = "image_id", insertable=false, updatable=false)
    protected Image image;


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
        this.inputSets = new HashSet<>();
        this.inputParams = new HashSet<>();
    }

    public Module(Image image, String name, String description, Collection<ModelSet> inputSets, Collection<ModelParameter> inputParams) {
        this.id = new ModuleId(image.getId(), name);
        this.description = description;
        this.image = image;
        this.inputSets = new HashSet<>(inputSets);
        this.inputParams = new HashSet<>(inputParams);
        for(ModelSet s : inputSets){
            s.setModuleName(this.getName());
        }
        for(ModelParameter p : inputParams){
            p.setModuleName(this.getName());
        }
    }

    public Module(Image image, String moduleName, String description, Set<SetDefinitionDTO> inputSets, Set<ParameterDefinitionDTO> inputParams) throws Exception {
        this.id = new ModuleId(image.getId(), moduleName);
        this.description = description;
        this.image = image;
        this.inputSets = new HashSet<>();
        this.inputParams = new HashSet<>();
        
        // Convert DTOs to model objects
        for (SetDefinitionDTO setDTO : inputSets) {
            this.addSet(setDTO);
            //consider adding s.setModuleName(this.getName());
        }
        
        for (ParameterDefinitionDTO paramDTO : inputParams) {
            this.addParam(paramDTO);
            //consider adding p.setModuleName(this.getName());
        }
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

    public Set<ModelSet> getInputSets() {
        return inputSets;
    }

    public Set<ModelParameter> getInputParams() {
        return inputParams;
    }

    public ModelComponent loadFullComponent(ModelComponent mc){
        return image.getModel().getComponent(mc.getIdentifier());
    }
    
    public abstract Set<ModelSet> getInvolvedSets();

    public abstract Set<ModelParameter> getInvolvedParameters();

    @Transactional
    public void addParam(ParameterDefinitionDTO paramDTO) throws Exception {
        ModelParameter param = image.getModel().getParameter(paramDTO.name());
        inputParams.add(param);
        param.setModuleName(this.getName());
        param.getModelComponentId().setImageId(this.getId());
        param.update(paramDTO);
    }

    @Transactional
    public void addSet(SetDefinitionDTO setDTO) throws Exception {
        ModelSet set = image.getModel().getSet(setDTO.name());
        inputSets.add(set);
        set.setModuleName(this.getName());
        set.getModelComponentId().setImageId(this.getId());
        set.update(setDTO);
    }

    //TODO: get rid of the following two methods, adding sets and params should be with DTOs
    @Transactional
    public void addParam(ModelParameter param) throws Exception {
        inputParams.add(param);
        param.setModuleName(this.getName());
        param.getModelComponentId().setImageId(this.getId());
    }
    @Transactional
    public void addSet(ModelSet set) throws Exception {
        inputSets.add(set);
        set.setModuleName(this.getName());
        set.getModelComponentId().setImageId(this.getId());
    }

    @Transactional
    public void removeSet(ModelSet set){
        inputSets.remove(set);
    }

    @Transactional
    public void removeParam(ModelParameter param){
        inputParams.remove(param);
    }

    /**
     * Checks if the given model input (set or parameter) is an input of this module
     * @param modelInputId The identifier of the model input to check
     * @return true if the input belongs to this module, false otherwise
     */
    public boolean isInput(String modelInputId) {
        return inputSets.stream().anyMatch(set -> set.getIdentifier().equals(modelInputId)) ||
               inputParams.stream().anyMatch(param -> param.getIdentifier().equals(modelInputId));
    }
}
