package com.indra.catalog.products.web;

import com.indra.catalog.products.application.ProductService;
import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products/{id} existente retorna 200, Content-Type y body correcto")
    void findById_whenProductExists_shouldReturn200AndProductResponse() throws Exception {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        when(productService.findById("PRD-0001")).thenReturn(product);

        mockMvc.perform(get("/api/products/PRD-0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(3500000.00))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("GET /api/products/{id} inexistente retorna 404")
    void findById_whenProductDoesNotExist_shouldReturn404() throws Exception {
        when(productService.findById("PRD-9999")).thenThrow(new ProductNotFoundException("PRD-9999"));

        mockMvc.perform(get("/api/products/PRD-9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/products con body válido y rol EDITOR retorna 201 y header Location")
    void create_whenRequestIsValid_shouldReturn201AndLocationHeader() throws Exception {
        Product created = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        when(productService.create(any(Product.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .with(httpBasic("editor", "editor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "price": 3500000.00,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", "http://localhost/api/products/PRD-0001"))
                .andExpect(jsonPath("$.id").value("PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(3500000.00))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("POST /api/products con nombre vacío retorna 400 y mensaje de error")
    void create_whenNameIsBlank_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(httpBasic("editor", "editor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "price": 3500000.00,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value(containsString("nombre")));

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} con rol ADMIN retorna 204 No Content")
    void delete_whenAdmin_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} inexistente con rol ADMIN retorna 404")
    void delete_whenProductDoesNotExist_shouldReturn404() throws Exception {
        doThrow(new ProductNotFoundException("PRD-9999")).when(productService).delete("PRD-9999");

        mockMvc.perform(delete("/api/products/PRD-9999")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/products sin autenticación retorna 401 Unauthorized")
    void create_whenAnonymous_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "price": 3500000.00,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} con rol EDITOR (no ADMIN) retorna 403 Forbidden")
    void delete_whenEditor_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001")
                        .with(httpBasic("editor", "editor123")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }
}

