package Image.Modules;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ModuleId implements Serializable {
    
    @Column(name = "image_id")
    private String imageId;

    @Column(name = "name")
    private String name;

    protected ModuleId() {} 

    public ModuleId(String imageId, String name) {
        this.imageId = imageId;
        this.name = name;
    }

    // Getters and setters
    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
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
        return Objects.equals(imageId, moduleId.imageId) &&
                Objects.equals(name, moduleId.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId, name);
    }
}
