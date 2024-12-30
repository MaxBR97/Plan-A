package Model;

public interface ModelType {
    public boolean isCompatible(ModelType type);
    public boolean isCompatible(String str);
}
