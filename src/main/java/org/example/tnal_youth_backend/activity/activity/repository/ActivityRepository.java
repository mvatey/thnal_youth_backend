package org.example.tnal_youth_backend.activity.activity.repository;

import org.example.tnal_youth_backend.activity.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository
        extends JpaRepository<Activity, Long> {

    List<Activity> findAllByOrderByStartsAtDescIdDesc();
}