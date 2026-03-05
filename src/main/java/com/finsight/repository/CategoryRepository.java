package com.finsight.repository;

import com.finsight.model.Category;
import com.finsight.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(TransactionType type);

    boolean existsByNameAndType(String name, TransactionType type);
}
