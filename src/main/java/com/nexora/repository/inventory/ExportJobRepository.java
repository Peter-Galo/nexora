package com.nexora.repository.inventory;

import com.nexora.model.inventory.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ExportJob entity.
 */
@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    /**
     * Find an export job by its unique job ID
     *
     * @param jobId the job ID to search for
     * @return an Optional containing the export job if found
     */
    Optional<ExportJob> findByJobId(String jobId);
}
