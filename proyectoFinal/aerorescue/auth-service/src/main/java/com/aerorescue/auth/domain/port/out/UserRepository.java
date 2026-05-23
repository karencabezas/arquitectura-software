package com.aerorescue.auth.domain.port.out;

import com.aerorescue.auth.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findById(UUID id);
    User save(User user);
}
