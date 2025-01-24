package Model;

import java.util.List;

public interface ModelType {
    public boolean isCompatible(ModelType type);
    public boolean isCompatible(String str);
    public String toString();
}
