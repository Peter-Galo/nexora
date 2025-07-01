package com.nexora.repository.inventory;

import com.nexora.model.inventory.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for ExportJob entity.
 */
@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, UUID> {
}
