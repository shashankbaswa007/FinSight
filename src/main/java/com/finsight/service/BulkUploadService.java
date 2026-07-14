package com.finsight.service;

import com.finsight.dto.BulkUploadCommitResponse;
import com.finsight.dto.BulkUploadErrorDto;
import com.finsight.dto.BulkUploadPreviewResponse;
import com.finsight.dto.BulkUploadRowDto;
import com.finsight.exception.BadRequestException;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BulkUploadService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final TransactionAuditService auditService;
    private final CategorizationEventPublisher categorizationEventPublisher;
    private final RagDocumentIngestionService ragDocumentIngestionService;

    private static final int MAX_ROWS = 1000;

    public BulkUploadService(TransactionRepository transactionRepository, UserRepository userRepository,
                             CategoryRepository categoryRepository, CategoryService categoryService,
                             TransactionAuditService auditService, CategorizationEventPublisher categorizationEventPublisher,
                             RagDocumentIngestionService ragDocumentIngestionService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.auditService = auditService;
        this.categorizationEventPublisher = categorizationEventPublisher;
        this.ragDocumentIngestionService = ragDocumentIngestionService;
    }

    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("date (YYYY-MM-DD)");
            header.createCell(1).setCellValue("amount");
            header.createCell(2).setCellValue("type (INCOME or EXPENSE)");
            header.createCell(3).setCellValue("category (optional)");
            header.createCell(4).setCellValue("description (optional)");

            // Instructions sheet
            Sheet instructions = workbook.createSheet("Instructions");
            Row row0 = instructions.createRow(0);
            row0.createCell(0).setCellValue("Bulk Upload Template Instructions");
            Row row1 = instructions.createRow(1);
            row1.createCell(0).setCellValue("1. Max rows allowed: " + MAX_ROWS);
            Row row2 = instructions.createRow(2);
            row2.createCell(0).setCellValue("2. Date format must be YYYY-MM-DD");
            Row row3 = instructions.createRow(3);
            row3.createCell(0).setCellValue("3. Valid types are INCOME or EXPENSE");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel template", e);
        }
    }

    public BulkUploadPreviewResponse parseAndValidate(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        List<BulkUploadRowDto> validRows = new ArrayList<>();
        List<BulkUploadErrorDto> errorRows = new ArrayList<>();
        List<BulkUploadRowDto> duplicateRows = new ArrayList<>();

        Map<String, Category> categoryCache = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c, (c1, c2) -> c1));

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 0;

            // Gather dates to check for duplicates later
            LocalDate minDate = LocalDate.MAX;
            LocalDate maxDate = LocalDate.MIN;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                rowCount++;
                if (rowCount > MAX_ROWS) {
                    break;
                }
                
                // Read cells
                Cell dateCell = row.getCell(0);
                Cell amountCell = row.getCell(1);
                Cell typeCell = row.getCell(2);
                Cell categoryCell = row.getCell(3);
                Cell descriptionCell = row.getCell(4);

                List<String> errors = new ArrayList<>();
                BulkUploadRowDto dto = new BulkUploadRowDto();
                dto.setRowNumber(row.getRowNum() + 1);

                // Date
                if (dateCell == null || dateCell.getCellType() == CellType.BLANK) {
                    errors.add("Date is required");
                } else {
                    try {
                        if (dateCell.getCellType() == CellType.NUMERIC) {
                            dto.setDate(dateCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                        } else {
                            dto.setDate(LocalDate.parse(dateCell.getStringCellValue().trim()));
                        }
                        if (dto.getDate().isBefore(minDate)) minDate = dto.getDate();
                        if (dto.getDate().isAfter(maxDate)) maxDate = dto.getDate();
                    } catch (Exception e) {
                        errors.add("Invalid date format. Use YYYY-MM-DD");
                    }
                }

                // Amount
                if (amountCell == null || amountCell.getCellType() == CellType.BLANK) {
                    errors.add("Amount is required");
                } else {
                    try {
                        double val = amountCell.getCellType() == CellType.NUMERIC ? amountCell.getNumericCellValue() : Double.parseDouble(amountCell.getStringCellValue().trim());
                        if (val <= 0) {
                            errors.add("Amount must be positive");
                        } else {
                            dto.setAmount(BigDecimal.valueOf(val));
                        }
                    } catch (Exception e) {
                        errors.add("Invalid amount format");
                    }
                }

                // Type
                if (typeCell == null || typeCell.getCellType() == CellType.BLANK) {
                    errors.add("Type is required");
                } else {
                    String t = typeCell.getStringCellValue().trim().toUpperCase();
                    if (t.equals("INCOME") || t.equals("EXPENSE")) {
                        dto.setType(t);
                    } else {
                        errors.add("Type must be INCOME or EXPENSE");
                    }
                }

                // Category
                if (categoryCell != null && categoryCell.getCellType() != CellType.BLANK) {
                    String catName = categoryCell.getStringCellValue().trim();
                    dto.setCategoryName(catName);
                    Category c = categoryCache.get(catName.toLowerCase());
                    if (c != null) {
                        dto.setCategoryId(c.getId());
                    }
                }

                // Description
                if (descriptionCell != null && descriptionCell.getCellType() != CellType.BLANK) {
                    String desc = descriptionCell.getCellType() == CellType.NUMERIC ? String.valueOf(descriptionCell.getNumericCellValue()) : descriptionCell.getStringCellValue().trim();
                    if (desc.length() > 500) {
                        errors.add("Description too long (max 500 chars)");
                    } else {
                        dto.setDescription(desc);
                    }
                }

                if (!errors.isEmpty()) {
                    errorRows.add(new BulkUploadErrorDto(dto.getRowNumber(), buildRawString(row), errors));
                } else {
                    validRows.add(dto);
                }
            }

            // Duplicate Detection
            if (!validRows.isEmpty() && minDate != LocalDate.MAX) {
                List<Transaction> existing = transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        userId, minDate.minusDays(1), maxDate.plusDays(1), org.springframework.data.domain.Pageable.unpaged()).getContent();
                
                Set<String> existingHashes = existing.stream()
                        .map(t -> hashTransaction(t.getDate(), t.getAmount(), t.getDescription()))
                        .collect(Collectors.toSet());

                Iterator<BulkUploadRowDto> it = validRows.iterator();
                while (it.hasNext()) {
                    BulkUploadRowDto rowDto = it.next();
                    String hash = hashTransaction(rowDto.getDate(), rowDto.getAmount(), rowDto.getDescription());
                    if (existingHashes.contains(hash)) {
                        duplicateRows.add(rowDto);
                        it.remove();
                    }
                }
            }

            return new BulkUploadPreviewResponse(validRows, errorRows, rowCount, validRows.size(), errorRows.size(), duplicateRows, duplicateRows.size());

        } catch (Exception e) {
            throw new BadRequestException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    @Transactional
    public BulkUploadCommitResponse commitUpload(Long userId, BulkUploadPreviewResponse preview) {
        User user = userRepository.findById(userId).orElseThrow();
        int createdCount = 0;
        
        List<Transaction> toSave = new ArrayList<>();
        List<Long> requiresCategorization = new ArrayList<>();

        for (BulkUploadRowDto row : preview.getValidRows()) {
            Category category = null;
            boolean needsAi = false;
            if (row.getCategoryId() != null) {
                category = categoryRepository.findById(row.getCategoryId()).orElse(null);
            }
            if (category == null) {
                category = categoryService.getOrCreateDefaultCategory();
                needsAi = true;
            }

            Transaction t = Transaction.builder()
                    .user(user)
                    .amount(row.getAmount())
                    .type(TransactionType.valueOf(row.getType()))
                    .category(category)
                    .description(row.getDescription())
                    .date(row.getDate())
                    .build();
            toSave.add(t);
        }

        List<Transaction> saved = transactionRepository.saveAll(toSave);
        createdCount = saved.size();

        for (Transaction t : saved) {
            auditService.logCreate(t, user.getEmail());
            if (t.getCategory().getName().equalsIgnoreCase("Uncategorized") || t.getCategory().getName().equalsIgnoreCase("Other")) {
                categorizationEventPublisher.publishCategorizationEvent(t.getId(), t.getDescription());
            }
        }

        triggerRagIngestion(userId);

        return new BulkUploadCommitResponse(createdCount, preview.getDuplicateCount() + preview.getErrorCount(), "Success");
    }

    private String buildRawString(Row row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            sb.append(c.toString()).append(" | ");
        }
        return sb.toString();
    }

    private String hashTransaction(LocalDate date, BigDecimal amount, String description) {
        String desc = description == null ? "" : description.trim().toLowerCase();
        return date.toString() + "_" + amount.stripTrailingZeros().toPlainString() + "_" + desc;
    }

    private void triggerRagIngestion(Long userId) {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ragDocumentIngestionService.ingestUserData(userId);
                    }
                }
            );
        } else {
            ragDocumentIngestionService.ingestUserData(userId);
        }
    }
}
