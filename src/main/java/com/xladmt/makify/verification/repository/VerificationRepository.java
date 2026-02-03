package com.xladmt.makify.verification.repository;

import com.xladmt.makify.common.entity.VerificationMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationMethod, Long> {

}
