package com.clickfarma.backend;

import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(
			UsuarioRepository usuarioRepository, 
			PasswordEncoder passwordEncoder,
			@org.springframework.beans.factory.annotation.Value("${ADMIN_EMAIL_1:admin1@clickfarma.com}") String adminEmail1,
			@org.springframework.beans.factory.annotation.Value("${ADMIN_PASSWORD_1:admin123click}") String adminPass1,
			@org.springframework.beans.factory.annotation.Value("${ADMIN_EMAIL_2:admin2@clickfarma.com}") String adminEmail2,
			@org.springframework.beans.factory.annotation.Value("${ADMIN_PASSWORD_2:admin123click}") String adminPass2) {
		return args -> {
			// Admin 1
			if (!usuarioRepository.existsByEmail(adminEmail1)) {
				Usuario admin1 = new Usuario();
				admin1.setNome("Douglas (Admin)");
				admin1.setEmail(adminEmail1);
				admin1.setSenha(passwordEncoder.encode(adminPass1));
				admin1.setRole("ADMIN");
				usuarioRepository.save(admin1);
				System.out.println("✅ Admin 1 criado!");
			}
			// Admin 2
			if (!usuarioRepository.existsByEmail(adminEmail2)) {
				Usuario admin2 = new Usuario();
				admin2.setNome("Gustavson (Admin)");
				admin2.setEmail(adminEmail2);
				admin2.setSenha(passwordEncoder.encode(adminPass2));
				admin2.setRole("ADMIN");
				usuarioRepository.save(admin2);
				System.out.println("✅ Admin 2 criado!");
			}
		};
	}
}
