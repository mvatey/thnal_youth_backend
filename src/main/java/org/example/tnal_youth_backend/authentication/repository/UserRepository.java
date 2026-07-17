package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneOrEmail(String phone, String email);

    @Query(value = """
        SELECT m.branch_id
        FROM users u
        JOIN members m
            ON m.id = u.member_id
        WHERE u.id = :userId
    """, nativeQuery = true)
    Optional<Long> findBranchIdByUserId(
            @Param("userId") Long userId
    );
}