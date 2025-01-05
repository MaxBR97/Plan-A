package Model;

import java.util.List;

public abstract class ModelOutput extends ModelComponent {
    protected List<ModelSet> dependency;
    protected boolean isComplex;
    public ModelOutput(String identifier) {
        super(identifier);
    }

    public ModelOutput(String ident, List<ModelSet> dep){
        super(ident);
        dependency = dep;
    }

    public List<ModelSet> getDependencies () {
        return dependency;
    }
}