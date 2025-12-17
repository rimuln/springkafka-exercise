package navrat.name.vivicta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"navrat.name.vivicta"})
public class OrderApplication {

	public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
	}
}
