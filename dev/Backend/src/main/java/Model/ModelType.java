package Model;

import java.util.LinkedList;
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
    public static String[] convertStringToAtoms(String element){
        List<String> ans = new LinkedList<>();
        if(element.matches("\".*\"")){
            return new String[]{element.substring(1, element.length()-1)};
        } else if (element.matches("<.*>")){
            // Remove the outer angle brackets
            String content = element.substring(1, element.length()-1);
            
            // Split by comma, but only when not inside quotes
            // This regex matches commas that are not inside quotes
            String[] splitted = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            for(String subPart : splitted){
                String trimmedPart = subPart.trim();
                String[] tmp = convertStringToAtoms(trimmedPart);
                ans.addAll(List.of(tmp));
            }
            return ans.toArray(new String[0]);
        } else {
            return new String[]{element};
        }
         
    }
}
