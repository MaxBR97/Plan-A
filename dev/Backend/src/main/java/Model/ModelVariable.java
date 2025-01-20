package Model;

import java.util.List;

import parser.FormulationParser.VariableContext;

public class ModelVariable extends ModelOutput {
    
    public ModelVariable(String identifier) {
        super(identifier);
    }

    public ModelVariable(String ident, List<ModelSet> dep){
        super(ident,dep);
    }

   
}