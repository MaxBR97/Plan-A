package Model;

import java.util.List;

public interface ModelType {
    public boolean isCompatible(ModelType type);
    public boolean isCompatible(String str);
    public String toString();
    List<String> typeList();
    //dynamic dispatch somewhat doesnt work with static methods...
    public static String convertArrayOfAtoms(String[] atoms, ModelType type){
         if(type instanceof Tuple)
            return Tuple.convertArrayOfAtoms(atoms,type);
        else    
            return ModelPrimitives.convertArrayOfAtoms(atoms,type);
    }   
}
