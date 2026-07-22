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

    /*
     * Count members whose member status code is INACTIVE.
     */
    @Query("""
            SELECT COUNT(m)
            FROM Member m
            JOIN m.status status
            WHERE UPPER(status.code) = 'INACTIVE'
            """)
    long countInactiveMembers();

    /*
     * A leader is a member linked to a User account whose role is
     * BRANCH_LEADER.
     *
     * Relationship:
     * users.member_id -> members.id
     */
    @Query(
            value = """
                    SELECT COUNT(DISTINCT u.member_id)
                    FROM users u
                    INNER JOIN members m
                        ON m.id = u.member_id
                    WHERE UPPER(u.role) = 'BRANCH_LEADER'
                      AND u.member_id IS NOT NULL
                    """,
            nativeQuery = true
    )
    long countLeaderMembers();
}