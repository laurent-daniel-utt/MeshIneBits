package meshIneBits.webUI.server;
		import org.springframework.boot.SpringApplication;
		import org.springframework.boot.autoconfigure.SpringBootApplication;
		import org.springframework.web.bind.annotation.GetMapping;
		import org.springframework.context.annotation.ComponentScan;
		import org.springframework.stereotype.Controller;

@SpringBootApplication
@Controller
@ComponentScan(basePackages = "meshIneBits.webUI.server")
public class WebApp {
	public static void main(String[] args) {
		SpringApplication.run(WebApp.class, args);
	}

	@GetMapping ("/")
	public String hello() {
		return "index";
	}
}

