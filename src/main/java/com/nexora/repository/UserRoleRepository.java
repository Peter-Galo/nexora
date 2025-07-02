package com.nexora.repository;

import com.nexora.model.Role;
import com.nexora.model.User;
import com.nexora.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUser(User user);
    Optional<UserRole> findByUserAndRole(User user, Role role);
    void deleteByUserAndRole(User user, Role role);
}