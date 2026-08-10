package org.example.tnal_youth_backend.document.option.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "document_options")
@Getter
@Setter
public class DocumentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String value;

    @Column(name = "label_km", nullable = false, length = 100)
    private String labelKm;

    @Column(name = "label_en", nullable = false, length = 100)
    private String labelEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
