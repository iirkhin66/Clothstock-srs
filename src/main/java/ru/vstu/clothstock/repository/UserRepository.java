package ru.vstu.clothstock.repository;

import ru.vstu.clothstock.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring сам напишет SQL-запрос для поиска по email
    Optional<User> findByEmail(String email);
}
