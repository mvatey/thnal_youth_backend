package org.example.tnal_youth_backend.branch.repository;

import org.example.tnal_youth_backend.branch.model.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query(value = """
        WITH RECURSIVE branch_tree AS (

            SELECT id, parent_branch_id
            FROM branches
            WHERE id = :rootBranchId

            UNION

            SELECT b.id, b.parent_branch_id
            FROM branches b
            INNER JOIN branch_tree bt
                ON b.parent_branch_id = bt.id

        )

        SELECT id
        FROM branch_tree
        ORDER BY id
        """, nativeQuery = true)
    List<Long> findBranchAndDescendantIds(
            @Param("rootBranchId") Long rootBranchId
    );

}