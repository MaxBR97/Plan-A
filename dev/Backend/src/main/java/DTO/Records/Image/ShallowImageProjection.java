package DTO.Records.Image;

public interface ShallowImageProjection {
    String getId();
    String getName();
    String getDescription();
    String getOwner();
    Boolean getIsPrivate();
    Boolean getIsConfigured();

}
