package Acceptance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import groupId.Main;

// @SpringBootTest(
//     webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//     classes = Main.class,
//     properties = {
//         "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
//     }
// )
// @EmbeddedKafka(partitions = 1, topics = {"solve_request", "compile_request"})
// @ActiveProfiles({"H2mem", "kafkaSolver", "S3-test","securityAndGateway"})
// public class KafkaServiceTest extends ServiceTest {
// }

