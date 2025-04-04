package Acceptance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import DTO.Records.Requests.Commands.CreateImageFromFileDTO;

public class CreateImageRequestBuilder implements RequestBuilder {
    CreateImageFromFileDTO req;

    public CreateImageRequestBuilder(String imageName,String imageDescription, String code){
        req = new CreateImageFromFileDTO(imageName,imageDescription,code);
    }

    public CreateImageRequestBuilder(String imageName,String imageDescription, Path code){
         String codeString = "";
            try {
            codeString = Files.readString(code);
            
            } catch (Exception e) {
                e.printStackTrace();
                assertTrue(false);
            }
        req = new CreateImageFromFileDTO(imageName,imageDescription,codeString);
    }
    
    public CreateImageFromFileDTO build() {
       return req;
    }
    
}
