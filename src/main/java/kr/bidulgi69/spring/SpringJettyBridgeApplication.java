package kr.bidulgi69.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "kr.bidulgi69")
public class SpringJettyBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJettyBridgeApplication.class, args);
    }
}
