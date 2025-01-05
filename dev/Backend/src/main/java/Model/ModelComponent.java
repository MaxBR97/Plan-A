package Model;

public abstract class ModelComponent {
    protected String identifier;
    
    public ModelComponent(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}

