package Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Tuple implements ModelType {
    private List<ModelPrimitives> val;

     @JsonCreator
    public Tuple(@JsonProperty("types") List<ModelPrimitives> val) {
        this.val = val;
    }
    
    public Tuple(ModelPrimitives[] val){
        this.val = new ArrayList<>(Arrays.asList(val));
    }
    

    Tuple() {
        val = new LinkedList();
    }

    public List<ModelPrimitives> getTypes(){
        return val;
    }

    public boolean isCompatible(ModelType p){
        if(p instanceof Tuple){
            if(((Tuple)p).val.size() == this.val.size()){
                Iterator<ModelPrimitives> it1 = ((Tuple)p).val.iterator();
                Iterator<ModelPrimitives> it2 = val.iterator();
                while(it1.hasNext()){
                    if(it1.next() != it2.next())
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isCompatible(String str) {

        if (str == null || str.length() < 2 || !str.startsWith("<") || !str.endsWith(">")) {
            return false;
        }
        
        
        String content = str.substring(1, str.length() - 1).trim();
        
        
        if (content.isEmpty()) {
            return val.isEmpty();
        }
        

        String[] elements = content.split(",");
        

        if (elements.length != val.size()) {
            return false;
        }
        
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i].trim();
            ModelPrimitives currentPrimitive = val.get(i);
            
            if (!currentPrimitive.isCompatible(element)) {
                return false;
            }
        }
        
        return true;
    }

    void append(ModelType tmp) {
        if(tmp instanceof ModelPrimitives)
            val.add((ModelPrimitives)tmp);
        else {
            for(ModelPrimitives p : ((Tuple)tmp).getTypes())
                val.add(p);
        }

    }

    public String toString() {
        if (val == null || val.isEmpty()) {
            return "<>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        
        for (int i = 0; i < val.size(); i++) {
            sb.append(val.get(i).toString());
            if (i < val.size() - 1) {
                sb.append(",");
            }
        }
        
        sb.append('>');
        return sb.toString();
    }
    @Override
    public List<String> typeList(){
        List<String> types = new LinkedList<>();
        for(ModelPrimitives primitives:val){
            types.add(primitives.toString());
        }
        return types;
    }
    public int size() {
        return this.val.size();
    }

    
    public static String convertArrayOfAtoms(String[] atoms, ModelType type) {
        Tuple tup = ((Tuple)type);
        String ans = "";
        for(int i = 0; i < atoms.length ; i++){
            ans += ModelPrimitives.convertArrayOfAtoms(new String[] {atoms[i]},tup.getTypes().get(i)) + ",";
        }
        ans = ans.substring(0, ans.length()-1);
        if(atoms.length == 1)
            return ans;
        return "<" + ans + ">";
    }
    
}

