package Model;
import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ModelComponentId implements Serializable {
    @Column(name="name",  length = 450)
    private String identifier;
    
    @Column(name="image_id", insertable=false, updatable=false)
    private String imageId;

    // Default constructor (required by Hibernate)
    public ModelComponentId() {
        identifier = "defaultIdentifier";
        imageId = "defaultImageId";
    }

    public ModelComponentId(String identifier, String imageId) {
        this.identifier = identifier;
        this.imageId = imageId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelComponentId that = (ModelComponentId) o;
        return Objects.equals(identifier, that.identifier) &&
               Objects.equals(imageId, that.imageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, imageId);
    }

    @Override
    public String toString() {
        return "ModelComponentId{" +
                "identifier='" + identifier + '\'' +
                ", imageId='" + imageId + '\'' +
                '}';
    }
}
