package Model;
public abstract class ModelInput extends ModelComponent {
    private ModelType myType;

    public ModelInput(String identifier, ModelType type) {
        super(identifier);
        myType = type;
    }

    public ModelType getType(){
        return myType;
    }

    public boolean isCompatible(ModelType val){
        return myType.isCompatible(val);
    }

    public boolean isCompatible(String str){
        if(myType == ModelPrimitives.UNKNOWN)
            return true;
        return myType.isCompatible(str);
    }
}