package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.UserBranchAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface UserBranchAssignmentRepository
        extends JpaRepository<UserBranchAssignment, Long> {

    List<UserBranchAssignment> findByUserId(Long userId);

    @Query(
            "SELECT uba.branchId "
                    + "FROM UserBranchAssignment uba "
                    + "WHERE uba.userId = :userId"
    )
    Set<Long> findBranchIdsByUserId(
            @Param("userId") Long userId
    );

    @Transactional
    void deleteByUserId(Long userId);
}
