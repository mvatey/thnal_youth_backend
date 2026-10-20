package org.example.tnal_youth_backend.activity.expense.repository;

import org.example.tnal_youth_backend.activity.expense.entity.ActivityExpenseFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityExpenseFamilyRepository
        extends JpaRepository<ActivityExpenseFamily, Long> {

    List<ActivityExpenseFamily> findAllByActivityIdOrderByIdAsc(Long activityId);

    Optional<ActivityExpenseFamily> findByIdAndActivityId(Long id, Long activityId);
}
