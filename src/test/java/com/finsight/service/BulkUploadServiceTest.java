package com.finsight.service;

import com.finsight.dto.BulkUploadPreviewResponse;
import com.finsight.exception.BadRequestException;
import com.finsight.model.Category;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BulkUploadServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private TransactionAuditService auditService;
    @Mock
    private CategorizationEventPublisher categorizationEventPublisher;
    @Mock
    private RagDocumentIngestionService ragDocumentIngestionService;

    @InjectMocks
    private BulkUploadService bulkUploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateTemplate() {
        byte[] template = bulkUploadService.generateTemplate();
        assertNotNull(template);
        assertTrue(template.length > 0);
    }

    @Test
    void testParseEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        assertThrows(BadRequestException.class, () -> bulkUploadService.parseAndValidate(1L, file));
    }

    @Test
    void testParseAndValidate_ValidRow() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Transactions");
        Row header = sheet.createRow(0); // Header
        Row data = sheet.createRow(1);
        data.createCell(0).setCellValue("2026-07-15");
        data.createCell(1).setCellValue(150.0);
        data.createCell(2).setCellValue("EXPENSE");
        data.createCell(3).setCellValue("Food");
        data.createCell(4).setCellValue("Groceries");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());

        Category mockCategory = new Category();
        mockCategory.setId(10L);
        mockCategory.setName("Food");
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(mockCategory));
        when(transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        BulkUploadPreviewResponse response = bulkUploadService.parseAndValidate(1L, file);
        
        assertEquals(1, response.getValidCount());
        assertEquals(0, response.getErrorCount());
        assertEquals(0, response.getDuplicateCount());
        assertEquals("Groceries", response.getValidRows().get(0).getDescription());
        assertEquals(10L, response.getValidRows().get(0).getCategoryId());
    }
}
