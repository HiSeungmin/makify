package com.xladmt.makify.challenge.repository;

import com.xladmt.makify.common.entity.VerificationMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationMethodRepository extends JpaRepository<VerificationMethod, Long> {
}
