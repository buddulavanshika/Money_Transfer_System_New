package com.mts.application.repository;

import com.mts.application.entities.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, String> {
}
