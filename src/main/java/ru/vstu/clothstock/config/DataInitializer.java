package ru.vstu.clothstock.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.vstu.clothstock.model.User;
import ru.vstu.clothstock.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User(null, "Администратор", "admin@vstu.ru", passwordEncoder.encode("1234"), "ROLE_ADMIN");
                User manager = new User(null, "Менеджер", "manager@vstu.ru", passwordEncoder.encode("1234"), "ROLE_MANAGER");
                User cashier = new User(null, "Кассир", "cashier@vstu.ru", passwordEncoder.encode("1234"), "ROLE_CASHIER");

                userRepository.save(admin);
                userRepository.save(manager);
                userRepository.save(cashier);
            }
        };
    }
}