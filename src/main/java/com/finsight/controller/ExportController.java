package com.finsight.controller;

import com.finsight.service.ExportService;
import com.finsight.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
@Tag(name = "Export", description = "Export financial data")
public class ExportController {

    private final ExportService exportService;
    private final SecurityUtil securityUtil;

    public ExportController(ExportService exportService, SecurityUtil securityUtil) {
        this.exportService = exportService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/transactions/csv")
    @Operation(summary = "Export transactions as CSV")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = securityUtil.getCurrentUserId();
        byte[] csv = exportService.exportTransactionsCsv(userId, startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
