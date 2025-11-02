package vn.hoidanit.laptopshop;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Hello world";
    }

    @GetMapping("/user")
    public String userPage() {
        return "Only user can access this page";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "Only admin can access this page";
    }
}
