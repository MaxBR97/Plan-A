package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Model.ModelInput.StructureBlock;

public abstract class ModelOutput extends ModelComponent {
    protected boolean isComplex;

    public ModelOutput(String identifier) {
        super(identifier);
    }

    public ModelOutput(String ident, List<ModelSet> dep, List<ModelParameter> paramDep) {
        super(ident,dep,paramDep);
        isComplex = false;
    }


    /**
     * @return true if this ModelOutput is complex, false otherwise
     */
    public boolean isComplex() {
        return isComplex;
    }

    public StructureBlock[] getStructure(){
        List<StructureBlock> sb = new LinkedList<>();
        for(ModelSet s : this.setDependencies){
            sb.addAll(List.of(s.getStructure()));
        }

        return ((StructureBlock[])sb.toArray());
    }
}