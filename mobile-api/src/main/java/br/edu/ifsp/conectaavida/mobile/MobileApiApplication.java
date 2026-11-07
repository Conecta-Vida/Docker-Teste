package br.edu.ifsp.conectaavida.mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * MOTOR DE ARRANQUE DA MOBILE API (Para o App Flutter)
 */
@SpringBootApplication
@EntityScan(basePackages = "br.edu.ifsp.conectaavida.core.domain")
@EnableJpaRepositories(basePackages = "br.edu.ifsp.conectaavida.core.repository")
@ComponentScan(basePackages = {
        "br.edu.ifsp.conectaavida.mobile", // Lê as rotas desta API
        "br.edu.ifsp.conectaavida.core"    // Lê os compartilhamentos (ex: PushTokenRegistry)
})
public class MobileApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileApiApplication.class, args);
    }
}