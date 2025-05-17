package Unit.Model;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import SolverService.SolverApplication;
import groupId.Main;

@ActiveProfiles({"H2mem","grpcSolver","securityAndGateway"})
public class RemoteModelTest  extends ModelTest {
    
}
