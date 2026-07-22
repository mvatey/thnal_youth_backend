package org.example.tnal_youth_backend.activity.activity.repository;

import org.example.tnal_youth_backend.activity.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityRepository
        extends JpaRepository<Activity, Long> {

    /*
     * Get all activities.
     */
    List<Activity> findAllByOrderByStartsAtDescIdDesc();

    /*
     * Search by Khmer title or English title.
     */
    @Query("""
            SELECT a
            FROM Activity a
            WHERE
                a.titleKm LIKE CONCAT('%', :search, '%')
                OR LOWER(COALESCE(a.titleEn, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY
                a.startsAt DESC,
                a.id DESC
            """)
    List<Activity> searchByTitle(
            @Param("search")
            String search
    );

    /*
     * Filter by activity type ID.
     */
    List<Activity>
    findAllByTypeIdOrderByStartsAtDescIdDesc(
            Short typeId
    );
}