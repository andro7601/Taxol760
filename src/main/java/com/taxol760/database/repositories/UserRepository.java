package com.taxol760.database.repositories;

import com.taxol760.database.models.user.UserModel;
import com.taxol760.database.models.user.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserModel> findByRole(UserRole role);
}
