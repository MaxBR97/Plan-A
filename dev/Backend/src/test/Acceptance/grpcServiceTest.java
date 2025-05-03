package Acceptance;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"H2mem", "grpcSolver", "S3-test"})
public class grpcServiceTest extends ServiceTest {
}
