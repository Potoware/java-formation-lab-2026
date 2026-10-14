package com.indra.catalog.products.application;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    @DisplayName("Debe generar id correlativo y guardar el producto")
    void create_shouldGenerateIdAndSaveProduct() {
        Product input = new Product(null, "Laptop", new BigDecimal("3500000.00"), 10);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.create(input);

        assertThat(result.getId()).isEqualTo("PRD-0001");
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(repository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe retornar el producto si existe en el repositorio")
    void findById_whenProductExists_shouldReturnProduct() {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        when(repository.findById("PRD-0001")).thenReturn(Optional.of(product));

        Product result = service.findById("PRD-0001");

        assertThat(result).isEqualTo(product);
        verify(repository).findById("PRD-0001");
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException si no existe en el repositorio")
    void findById_whenProductDoesNotExist_shouldThrowException() {
        when(repository.findById("PRD-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("PRD-9999");
    }

    @Test
    @DisplayName("Debe eliminar el producto si existe")
    void delete_whenProductExists_shouldDeleteProduct() {
        when(repository.existsById("PRD-0001")).thenReturn(true);

        service.delete("PRD-0001");

        verify(repository).deleteById("PRD-0001");
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException al eliminar si no existe")
    void delete_whenProductDoesNotExist_shouldThrowException() {
        when(repository.existsById("PRD-9999")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("PRD-9999");
    }
}

