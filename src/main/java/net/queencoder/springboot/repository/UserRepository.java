package net.queencoder.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.queencoder.springboot.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}
