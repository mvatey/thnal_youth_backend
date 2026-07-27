package org.example.tnal_youth_backend.donation.paymentmethod.repository;

import org.example.tnal_youth_backend.donation.paymentmethod.model.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository
        extends JpaRepository<PaymentMethod, Short> {

    List<PaymentMethod> findAllByOrderBySortOrderAscIdAsc();

    List<PaymentMethod>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();

    Optional<PaymentMethod> findByCodeIgnoreCase(String code);
}