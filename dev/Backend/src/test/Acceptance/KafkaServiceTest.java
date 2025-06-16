package Acceptance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import groupId.Main;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
// @ActiveProfiles({"H2mem", "securityAndGateway", "kafkaSolver", "test", "S3-test"})
// @TestPropertySource(properties = {
//     "app.file.storage-dir=../Test/Models",
//     "logging.level.org.springframework.security=DEBUG",
//     "logging.level.Acceptance=DEBUG",
//     "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/test",
//     "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/test/protocol/openid-connect/certs",
//     "spring.security.oauth2.resourceserver.jwt.audiences=test-client"
// })
// // @EmbeddedKafka(partitions = 1, topics = {"solve_request", "compile_request"})
// public class KafkaServiceTest extends ServiceTest {
// }

