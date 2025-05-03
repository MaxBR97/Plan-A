package Acceptance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import DTO.Records.Model.ModelData.InputDTO;
import DTO.Records.Requests.Commands.CreateImageFromFileDTO;
import DTO.Records.Requests.Commands.SolveCommandDTO;
import DTO.Records.Requests.Responses.CreateImageResponseDTO;

public class SolveCommandRequestBuilder implements RequestBuilder {
    SolveCommandDTO req;

    public SolveCommandRequestBuilder(CreateImageResponseDTO context){
        req = new SolveCommandDTO(context.imageId(),new InputDTO(new HashMap<>(), new HashMap<>(), new LinkedList<>(),new LinkedList<>()),10, "");
    }

    public SolveCommandRequestBuilder(String imageId){
        req = new SolveCommandDTO(imageId,new InputDTO(new HashMap<>(), new HashMap<>(), new LinkedList<>(),new LinkedList<>()),10, "");
    }

    public SolveCommandRequestBuilder setTimeout(int timeout){
        req = new SolveCommandDTO(req.imageId(), req.input(), timeout , "");
        return this;
    }

    public SolveCommandRequestBuilder setSetInput(String setId, List<List<String>> inputs){
        req.input().setsToValues().put(setId, inputs);
        return this;
    }

    public SolveCommandRequestBuilder setParamInput(String paramId, List<String> input){
        req.input().paramsToValues().put(paramId, input);
        return this;
    }

    public SolveCommandRequestBuilder addToggleOffConstraintModule(String moduleName){
        req.input().constraintModulesToggledOff().add(moduleName);
        return this;
    }

    public SolveCommandRequestBuilder addToggleOffPreferenceModule(String moduleName){
        req.input().preferenceModulesToggledOff().add(moduleName);
        return this;
    }
    
    public SolveCommandRequestBuilder setInput(InputDTO inputs){
        req = new SolveCommandDTO(req.imageId(),inputs,req.timeout(), "");
        return this;
    }

    public SolveCommandDTO build(){
        return req;
    }

    
}
