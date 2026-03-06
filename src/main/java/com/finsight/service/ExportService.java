package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExportService {

    private final TransactionRepository transactionRepository;

    public ExportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public byte[] exportTransactionsCsv(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndTypeAndDateBetween(userId, null, startDate, endDate);
            // Use custom query instead - get all types
            transactions = transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                    userId, startDate, endDate, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else {
            transactions = transactionRepository.findByUserIdOrderByDateDesc(
                    userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // CSV header
        writer.println("Date,Type,Category,Amount,Description");

        for (Transaction tx : transactions) {
            writer.printf("%s,%s,%s,%s,\"%s\"%n",
                    tx.getDate(),
                    tx.getType(),
                    tx.getCategory().getName(),
                    tx.getAmount().toPlainString(),
                    escapeCsv(tx.getDescription()));
        }

        writer.flush();
        return out.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
