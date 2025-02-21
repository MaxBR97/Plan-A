package Unit;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "model.useRemote=remote",
    "model.grpc.port=localhost",
    "model.grpc.port=0"
})
public class RemoteModelTest  extends ModelTest{
    
}
