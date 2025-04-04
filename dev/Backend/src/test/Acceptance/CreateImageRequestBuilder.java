package Acceptance;

import DTO.Records.Requests.Commands.CreateImageFromFileDTO;

public class CreateImageRequestBuilder implements RequestBuilder {
    CreateImageFromFileDTO req;

    public CreateImageRequestBuilder(String imageName,String imageDescription, String code){
        req = new CreateImageFromFileDTO(imageName,imageDescription,code);
    }
    
    public CreateImageFromFileDTO build() {
       return req;
    }
    
}
