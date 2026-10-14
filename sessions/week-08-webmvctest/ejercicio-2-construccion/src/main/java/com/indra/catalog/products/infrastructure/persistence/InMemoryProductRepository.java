package com.indra.catalog.products.infrastructure.persistence;

import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> database = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        database.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return database.containsKey(id);
    }

    @Override
    public void deleteById(String id) {
        database.remove(id);
    }
}

