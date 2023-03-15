package com.example.app.repository;

import com.example.app.model.User;
import com.example.app.model.User.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByIdAndRole(Long id, Role role);

  List<User> getUsersByRole(Role role);
}
