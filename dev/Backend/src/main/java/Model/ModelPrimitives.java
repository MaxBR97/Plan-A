package Model;

import java.util.List;

public enum ModelPrimitives implements ModelType{
    BINARY,
    TEXT,
    UNKNOWN, 
    INT,
    INFINITY,
    FLOAT;
    public boolean isCompatible(ModelType val){
        return this == val;
    }

    public boolean isCompatible(String str){
        switch(this) {
            case BINARY:
                return false;
            case TEXT:
              if (str.matches("\".*\""))
                return true;
              else
                return false;
            case INT:
              try{
                Integer.valueOf(str); 
                return true;
            } catch (Exception e){return false;}
              
            case INFINITY:
              return false;
              
            case FLOAT:
            try{
                Float.valueOf(str); 
                return true;
            } catch (Exception e){return false;} 
            default:
              return true;
          }
    }

    @Override
    public String toString() {
      switch(this) {
        case BINARY:
            return "BINARY";
        case TEXT:
          return "TEXT";
        case INT:
          return "INT";
          
        case INFINITY:
          return "INFINITY";
          
        case FLOAT:
          return "FLOAT";
        default:
          return "UNKNOWN";
      }
    }
    @Override
    public List<String> typeList(){
        return List.of(this.toString());
    }


}
