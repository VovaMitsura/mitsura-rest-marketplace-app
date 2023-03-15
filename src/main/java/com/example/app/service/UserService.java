package com.example.app.service;


import com.example.app.exception.UserNotFoundException;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsersByRole(Role role) {
    List<User> users = userRepository.getUsersByRole(role);

    if (users.isEmpty()) {
      throw new UserNotFoundException(String.format("no exists %ss in system", role));
    }

    return users;
  }

  public User getUserByIdAndRole(Long id, Role role) {
    Optional<User> user = userRepository.findByIdAndRole(id, role);

    return user.orElseThrow(
        () -> new UserNotFoundException(
            String.format("no exists %s with id: %d in system", role, id)));
  }
}
