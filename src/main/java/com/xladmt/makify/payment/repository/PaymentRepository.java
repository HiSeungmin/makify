package com.xladmt.makify.payment.repository;

import com.xladmt.makify.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
