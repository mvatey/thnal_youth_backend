package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberPaymentDto;
import org.example.tnal_youth_backend.member.entity.MemberPayment;
import org.example.tnal_youth_backend.member.service.MemberPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-payments")
public class MemberPaymentController {

    private final MemberPaymentService service;

    public MemberPaymentController(MemberPaymentService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberPayment> getPaymentsByMember(@PathVariable Long memberId) {
        return service.getPaymentsByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberPayment getPaymentById(@PathVariable Long id) {
        return service.getPaymentById(id);
    }

    @PostMapping
    public MemberPayment createPayment(@RequestBody MemberPaymentDto dto) {
        return service.createPayment(dto);
    }

    @PutMapping("/{id}")
    public MemberPayment updatePayment(@PathVariable Long id, @RequestBody MemberPaymentDto dto) {
        return service.updatePayment(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        service.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
