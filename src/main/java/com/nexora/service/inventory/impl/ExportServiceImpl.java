package com.nexora.service.inventory.impl;

import com.nexora.model.inventory.ExportJob;
import com.nexora.repository.inventory.ExportJobRepository;
import com.nexora.service.inventory.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ExportServiceImpl implements ExportService {

    private final ExportJobRepository exportJobRepository;
    private final S3Client s3Client;

    @Value("${do.spaces.bucket}")
    private String bucketName;

    @Value("${do.spaces.public-url}")
    private String spacesPublicUrl;

    @Autowired
    public ExportServiceImpl(ExportJobRepository exportJobRepository, S3Client s3Client) {
        this.exportJobRepository = exportJobRepository;
        this.s3Client = s3Client;
    }

    public String initiateExport(String userId, String exportType) {
        String jobId = UUID.randomUUID().toString();

        ExportJob job = new ExportJob();
        job.setJobId(jobId);
        job.setUserId(userId);
        job.setExportType(exportType);
        job.setStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());

        exportJobRepository.save(job);

        return jobId;
    }

    public void uploadToSpaces(String jobId, String userId, byte[] fileData, String filename) {
        try {
            String key = "exports/" + userId + "/" + filename;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    // Add content type for XLSX files
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(fileData));

            String fileUrl = spacesPublicUrl + "/" + key;

            // Update job with file URL
            ExportJob job = exportJobRepository.findByJobId(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            job.setFileUrl(fileUrl);
            job.setStatus("COMPLETED");
            job.setUpdatedAt(LocalDateTime.now());
            exportJobRepository.save(job);

        } catch (Exception e) {
            // Update job with error
            ExportJob job = exportJobRepository.findByJobId(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.setUpdatedAt(LocalDateTime.now());
            exportJobRepository.save(job);

            throw new RuntimeException("Failed to upload file to Digital Ocean Spaces", e);
        }
    }
}
