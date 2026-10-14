package com.indra.catalog.suppliers.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierTest {

    @Test
    void normalize_shouldUpperCaseNameAndTrim() {
        Supplier supplier = new Supplier();
        supplier.setName("   acme ltda   ");

        supplier.normalize();

        assertThat(supplier.getName()).isEqualTo("ACME LTDA");
    }

    @Test
    void normalize_shouldRemoveDotsFromTaxIdAndTrim() {
        Supplier supplier = new Supplier();
        supplier.setTaxId("  900.123.456-7  ");

        supplier.normalize();

        assertThat(supplier.getTaxId()).isEqualTo("900123456-7");
    }

    @Test
    void normalize_whenFieldsAreNull_shouldNotThrowException() {
        Supplier supplier = new Supplier();

        supplier.normalize();

        assertThat(supplier.getName()).isNull();
        assertThat(supplier.getTaxId()).isNull();
    }
}

