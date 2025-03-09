package Image.Modules;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ModuleId implements Serializable {
    
    @Column(name = "image_id", insertable=false, updatable=false)
    private String id;

    @Column(name = "name",insertable=false, updatable=false)
    private String name;

    protected ModuleId() {
        id = "noId";
        name= "noname";
    } 

    public ModuleId(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleId moduleId = (ModuleId) o;
        return Objects.equals(id, moduleId.id) &&
                Objects.equals(name, moduleId.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
