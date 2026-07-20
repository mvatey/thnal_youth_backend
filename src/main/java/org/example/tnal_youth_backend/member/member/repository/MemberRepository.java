package org.example.tnal_youth_backend.member.member.repository;

import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    boolean existsByMemberNoIgnoreCase(
            String memberNo
    );

    boolean existsByMemberNoIgnoreCaseAndIdNot(
            String memberNo,
            Long id
    );

    boolean existsByPhone(
            String phone
    );

    boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    @EntityGraph(attributePaths = {
            "status",
            "level",
            "religion",
            "profilePhoto",
            "cvFile"
    })
    @Query("""
            SELECT m
            FROM Member m
            WHERE m.id = :id
            """)
    Optional<Member> findDetailedById(
            @Param("id") Long id
    );

    @EntityGraph(attributePaths = {
            "status",
            "level",
            "profilePhoto"
    })
    @Query("""
            SELECT m
            FROM Member m
            ORDER BY m.createdAt DESC
            """)
    List<Member> findAllDetailed();
}