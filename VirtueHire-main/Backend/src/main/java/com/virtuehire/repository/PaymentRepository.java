package com.virtuehire.repository;

import com.virtuehire.model.Payment;
import com.virtuehire.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByHrId(Long hrId);
    List<Payment> findByHrIdAndStatus(Long hrId, PaymentStatus status);
    List<Payment> findByCandidateId(Long candidateId);
    Optional<Payment> findByPaymentGatewayId(String paymentGatewayId);
    List<Payment> findByStatus(PaymentStatus status);

    // For admin dashboard
    long countByStatus(PaymentStatus status);

    List<Payment> findTop5ByOrderByIdDesc();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = com.virtuehire.model.PaymentStatus.SUCCESS")
    Double sumSuccessfulPaymentAmount();
}
