package com.xladmt.makify.payment.repository;

import com.xladmt.makify.common.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
