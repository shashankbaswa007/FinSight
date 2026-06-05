package com.finsight.controller;

import com.finsight.dto.CurrencyResponse;
import com.finsight.service.CurrencyService;
import com.finsight.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
public class CurrencyController extends BaseController {
    
    private final CurrencyService currencyService;
    private final SecurityUtil securityUtil;
    
    public CurrencyController(CurrencyService currencyService, SecurityUtil securityUtil) {
        this.currencyService = currencyService;
        this.securityUtil = securityUtil;
    }
    
    /**
     * Get all active currencies
     */
    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> getActiveCurrencies() {
        securityUtil.getCurrentUserId(); // Verify authentication
        List<CurrencyResponse> currencies = currencyService.getActiveCurrencies()
            .stream()
            .map(CurrencyResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(currencies);
    }
}
