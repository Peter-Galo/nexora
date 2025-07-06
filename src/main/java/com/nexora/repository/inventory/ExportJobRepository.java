package com.nexora.repository.inventory;

import com.nexora.model.inventory.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ExportJob entity.
 */
@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, UUID> {

    /**
     * Find all export jobs for a specific user, ordered by creation date descending
     * @param userUuid the UUID of the user
     * @return list of export jobs for the user
     */
    List<ExportJob> findByUserUuidOrderByCreatedAtDesc(UUID userUuid);
}
