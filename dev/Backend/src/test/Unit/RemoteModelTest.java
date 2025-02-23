package Unit;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import SolverService.SolverServiceApplication;
import groupId.Main;

@ActiveProfiles({"H2mem","grpcSolver"})
public class RemoteModelTest  extends ModelTest {
    
}
