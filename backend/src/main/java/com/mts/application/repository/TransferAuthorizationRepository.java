package com.mts.application.repository;

import com.mts.application.entities.TransferAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferAuthorizationRepository extends JpaRepository<TransferAuthorization, Long> {
    List<TransferAuthorization> findByStatus(String status);
}
