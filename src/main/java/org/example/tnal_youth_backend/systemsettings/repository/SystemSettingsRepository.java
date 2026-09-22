package org.example.tnal_youth_backend.systemsettings.repository;

import org.example.tnal_youth_backend.systemsettings.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Short> {
}
