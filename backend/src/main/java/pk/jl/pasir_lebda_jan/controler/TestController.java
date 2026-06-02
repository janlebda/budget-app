package pk.jl.pasir_lebda_jan.controler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pk.jl.pasir_lebda_jan.model.AppInfo;

@RestController
public class TestController {
    
    @GetMapping("/api/test")
    public String test() {
        return "Hello from Spring Boot!";
    }
    @GetMapping("/api/info")
    public AppInfo getInfo() {
        return new AppInfo(
            "Aplikacja Budżetowa", 
            "1.0", 
            "Witaj w aplikacji budżetowej stworzonej ze Spring Boot!"
        );
    }
}
