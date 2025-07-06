package com.nexora.controller.inventory;

import com.nexora.model.inventory.Category;
import com.nexora.model.inventory.ExportJob;
import com.nexora.model.inventory.Status;
import com.nexora.repository.inventory.ExportJobRepository;
import com.nexora.security.JwtService;
import com.nexora.service.event.ExportMessageProducer;
import com.nexora.service.inventory.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryDataExportControllerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private ExportService exportService;

    @Mock
    private ExportMessageProducer exportMessageProducer;

    @Mock
    private ExportJobRepository exportJobRepository;

    @InjectMocks
    private InventoryDataExportController inventoryDataExportController;

    private MockMvc mockMvc;
    private UUID testUserId;
    private UUID testJobId;
    private ExportJob testExportJob;
    private List<ExportJob> testExportJobs;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryDataExportController).build();
        
        testUserId = UUID.randomUUID();
        testJobId = UUID.randomUUID();
        
        testExportJob = new ExportJob();
        testExportJob.setUuid(testJobId);
        testExportJob.setUserUuid(testUserId);
        testExportJob.setExportType("XLSX");
        testExportJob.setCategory(Category.PRODUCT);
        testExportJob.setStatus(Status.COMPLETED);
        testExportJob.setFileUrl("http://example.com/file.xlsx");
        testExportJob.setCreatedAt(LocalDateTime.now());
        testExportJob.setUpdatedAt(LocalDateTime.now());
        
        testExportJobs = Arrays.asList(testExportJob);
    }

    @Test
    void getUserExportJobs_withValidAuthHeader_shouldReturnExportJobs() throws Exception {
        // Given
        String authHeader = "Bearer valid-jwt-token";
        when(jwtService.extractUserUUIDFromAuthHeader(authHeader)).thenReturn(testUserId);
        when(exportJobRepository.findByUserUuidOrderByCreatedAtDesc(testUserId)).thenReturn(testExportJobs);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/export/jobs")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].uuid").value(testJobId.toString()))
                .andExpect(jsonPath("$[0].userUuid").value(testUserId.toString()))
                .andExpect(jsonPath("$[0].exportType").value("XLSX"))
                .andExpect(jsonPath("$[0].category").value("PRODUCT"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].fileUrl").value("http://example.com/file.xlsx"));
    }

    @Test
    void getUserExportJobs_withEmptyResult_shouldReturnEmptyArray() throws Exception {
        // Given
        String authHeader = "Bearer valid-jwt-token";
        when(jwtService.extractUserUUIDFromAuthHeader(authHeader)).thenReturn(testUserId);
        when(exportJobRepository.findByUserUuidOrderByCreatedAtDesc(testUserId)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/export/jobs")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}