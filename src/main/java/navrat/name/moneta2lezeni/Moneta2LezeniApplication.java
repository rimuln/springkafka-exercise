package navrat.name.moneta2lezeni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"navrat.name.vivicta"})
public class Moneta2LezeniApplication {

    static void main(String[] args) {
        SpringApplication.run(Moneta2LezeniApplication.class, args);
    }
}
