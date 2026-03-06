package com.finsight.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finsight.dto.CategoryRequest;
import com.finsight.dto.CategoryResponse;
import com.finsight.exception.BadRequestException;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.Category;
import com.finsight.model.TransactionType;
import com.finsight.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id(1L)
                .name("Food")
                .type(TransactionType.EXPENSE)
                .build();
    }

    @Test
    void createCategory_success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");
        request.setType(TransactionType.EXPENSE);

        when(categoryRepository.existsByNameAndType("Food", TransactionType.EXPENSE)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Food");
        assertThat(response.getType()).isEqualTo(TransactionType.EXPENSE);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_duplicateThrows() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");
        request.setType(TransactionType.EXPENSE);

        when(categoryRepository.existsByNameAndType("Food", TransactionType.EXPENSE)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getAllCategories_returnsList() {
        when(categoryRepository.findAll()).thenReturn(List.of(sampleCategory));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Food");
    }

    @Test
    void findById_found() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        Category result = categoryService.findById(1L);

        assertThat(result.getName()).isEqualTo("Food");
    }

    @Test
    void findById_notFoundThrows() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
