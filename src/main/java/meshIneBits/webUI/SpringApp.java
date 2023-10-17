package meshIneBits.webUI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(SpringApp.class)
                .run(args);
    }
}