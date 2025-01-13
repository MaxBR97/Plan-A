package Model;

import java.util.ArrayList;
import java.util.List;

import parser.FormulationParser.VariableContext;

public class ModelVariable extends ModelOutput {
    
    public ModelVariable(String identifier) {
        super(identifier);
    }

    public ModelVariable(String ident, List<ModelSet> dep){
        super(ident,dep);
    }

   public List<String> getLeafIdentifiers() {
       ArrayList<String> identifiers = new ArrayList<>();
        for (ModelSet modelSet: dependency){
            //TODO: Unclear order between params/sets in ModelSet, implementations ignores params

        }
        return identifiers;
   }
}