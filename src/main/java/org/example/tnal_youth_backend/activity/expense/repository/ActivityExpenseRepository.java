package org.example.tnal_youth_backend.activity.expense.repository;

import org.example.tnal_youth_backend.activity.expense.entity.ActivityExpense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityExpenseRepository
        extends JpaRepository<ActivityExpense, Long> {

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "exchangeRate",
                    "receiptFile",
                    "recordedBy"
            }
    )
    List<ActivityExpense>
    findAllByActivity_IdOrderByCreatedAtAsc(
            Long activityId
    );

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "exchangeRate",
                    "receiptFile",
                    "recordedBy"
            }
    )
    Optional<ActivityExpense>
    findByIdAndActivity_Id(
            Long expenseId,
            Long activityId
    );

    long countByActivity_Id(Long activityId);
}