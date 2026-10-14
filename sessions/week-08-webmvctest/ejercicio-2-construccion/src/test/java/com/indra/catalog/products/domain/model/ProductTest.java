package com.indra.catalog.products.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("Debe crear producto válido cuando los datos cumplen las invariantes")
    void shouldCreateProductWhenValid() {
        Product product = new Product("PRD-0001", "Laptop Pro", new BigDecimal("3500000.00"), 10);

        assertThat(product.getId()).isEqualTo("PRD-0001");
        assertThat(product.getName()).isEqualTo("Laptop Pro");
        assertThat(product.getPrice()).isEqualByComparingTo("3500000.00");
        assertThat(product.getStock()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Debe lanzar excepción si el nombre es vacío o blanco")
    void shouldThrowExceptionWhenNameIsBlank(String invalidName) {
        assertThatThrownBy(() -> new Product("PRD-0001", invalidName, new BigDecimal("100.00"), 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre es nulo")
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> new Product("PRD-0001", null, new BigDecimal("100.00"), 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre excede 120 caracteres")
    void shouldThrowExceptionWhenNameExceeds120Chars() {
        String longName = "A".repeat(121);
        assertThatThrownBy(() -> new Product("PRD-0001", longName, new BigDecimal("100.00"), 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("120 caracteres");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio es nulo, cero o negativo")
    void shouldThrowExceptionWhenPriceIsZeroOrNegative() {
        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", null, 5))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", BigDecimal.ZERO, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");

        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", new BigDecimal("-1.00"), 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el stock es negativo")
    void shouldThrowExceptionWhenStockIsNegative() {
        assertThatThrownBy(() -> new Product("PRD-0001", "Laptop", new BigDecimal("100.00"), -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock no puede ser negativo");
    }

    @Test
    @DisplayName("withId debe retornar una nueva instancia con el nuevo id")
    void shouldReturnNewInstanceWithId() {
        Product original = new Product(null, "Laptop", new BigDecimal("100.00"), 5);
        Product withId = original.withId("PRD-0001");

        assertThat(withId.getId()).isEqualTo("PRD-0001");
        assertThat(withId.getName()).isEqualTo(original.getName());
        assertThat(withId.getPrice()).isEqualTo(original.getPrice());
        assertThat(withId.getStock()).isEqualTo(original.getStock());
    }
}

