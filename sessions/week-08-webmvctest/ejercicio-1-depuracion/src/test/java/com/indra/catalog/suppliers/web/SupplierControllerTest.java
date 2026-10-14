package com.indra.catalog.suppliers.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.indra.catalog.suppliers.application.SupplierService;
import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SupplierController.class)
@Import(SupplierWebMapper.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Test
    @DisplayName("GET /api/suppliers/{id} con proveedor existente retorna 200 y body correcto")
    void findById_whenSupplierExists_shouldReturn200AndSupplier() throws Exception {
        Supplier supplier = new Supplier();
        supplier.setId("SUP-001");
        supplier.setName("ACME LTDA");
        supplier.setTaxId("900123456-7");
        supplier.setEmail("compras@acme.co");
        when(supplierService.findById("SUP-001")).thenReturn(supplier);

        mockMvc.perform(get("/api/suppliers/SUP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("SUP-001"))
                .andExpect(jsonPath("$.name").value("ACME LTDA"))
                .andExpect(jsonPath("$.taxId").value("900123456-7"))
                .andExpect(jsonPath("$.email").value("compras@acme.co"));
    }

    @Test
    @DisplayName("GET /api/suppliers/{id} con proveedor inexistente retorna 404")
    void findById_whenSupplierDoesNotExist_shouldReturn404NotFound() throws Exception {
        when(supplierService.findById("SUP-999")).thenThrow(new SupplierNotFoundException("SUP-999"));

        mockMvc.perform(get("/api/suppliers/SUP-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/suppliers con body válido retorna 201 y header Location")
    void create_whenRequestIsValid_shouldReturn201CreatedAndLocationHeader() throws Exception {
        Supplier created = new Supplier();
        created.setId("SUP-001");
        created.setName("ACME LTDA");
        created.setTaxId("900123456-7");
        created.setEmail("compras@acme.co");
        when(supplierService.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "  acme ltda ",
                                    "taxId": "900.123.456-7",
                                    "email": "compras@acme.co"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/suppliers/SUP-001"))
                .andExpect(jsonPath("$.name").value("ACME LTDA"))
                .andExpect(jsonPath("$.taxId").value("900123456-7"));
    }

    @Test
    @DisplayName("POST /api/suppliers con nombre vacío retorna 400 y no llama al servicio")
    void create_whenNameIsBlank_shouldReturn400BadRequestAndNotCallService() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "taxId": "900.123.456-7",
                                    "email": "compras@acme.co"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value(containsString("name")));

        verifyNoInteractions(supplierService);
    }

    @Test
    @DisplayName("DELETE /api/suppliers/{id} con proveedor existente retorna 204")
    void delete_whenSupplierExists_shouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/suppliers/SUP-001"))
                .andExpect(status().isNoContent());
    }
}
