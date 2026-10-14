package com.indra.catalog.products.infrastructure.persistence;

import com.indra.catalog.products.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryProductRepositoryTest {

    private InMemoryProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
    }

    @Test
    @DisplayName("Debe guardar y recuperar producto correctamente")
    void shouldSaveAndFindProduct() {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);

        repository.save(product);

        Optional<Product> found = repository.findById("PRD-0001");
        assertThat(found).isPresent().contains(product);
        assertThat(repository.existsById("PRD-0001")).isTrue();
    }

    @Test
    @DisplayName("Debe eliminar producto correctamente")
    void shouldDeleteProduct() {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        repository.save(product);

        repository.deleteById("PRD-0001");

        assertThat(repository.findById("PRD-0001")).isEmpty();
        assertThat(repository.existsById("PRD-0001")).isFalse();
    }
}

