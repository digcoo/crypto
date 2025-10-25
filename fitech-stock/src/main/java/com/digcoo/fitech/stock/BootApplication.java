package com.digcoo.fitech.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**c
 *  Env: config=stock;warmup=snapshot
 */

@SpringBootApplication
@ComponentScan(basePackages = {"com.digcoo.fitech.stock,com.digcoo.fitech.common"})
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
