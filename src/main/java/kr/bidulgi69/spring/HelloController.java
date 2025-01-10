package kr.bidulgi69.spring;

import jakarta.servlet.ServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//  Handle http/1.x requests
@RestController
public class HelloController {

    @GetMapping("/")
    public String hello(ServletRequest request) {
        return "Greetings from server using " + request.getProtocol() + "!";
    }
}
