package groupid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static int counter = 0;
    @GetMapping("/string")
    public String helloWorld() {
        counter++;
        return "Hello, World! " + counter;
    }
}