package navrat.name.moneta2lezeni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication(scanBasePackages = {"navrat.name.moneta2lezeni"})
@EnableAspectJAutoProxy
public class Moneta2LezeniApplication {

    static void main(String[] args) {
        SpringApplication.run(Moneta2LezeniApplication.class, args);
    }
}
