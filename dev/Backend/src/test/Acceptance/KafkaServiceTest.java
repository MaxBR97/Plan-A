package Acceptance;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"H2mem", "kafkaSolver", "S3-test"})
public class KafkaServiceTest extends ServiceTest {
}
