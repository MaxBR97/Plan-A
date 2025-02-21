package Unit;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "model.useRemote=local",
    "model.grpc.port=localhost",
    "model.grpc.port=0"
})
public class LocalModelTest  extends ModelTest{
    
}


