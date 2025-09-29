package com.smartmedicare.dao;

import java.util.List;
import java.util.Optional;

public interface BaseDAO<T> {
    void save(T entity);
    Optional<T> findById(String id);
    List<T> findAll();
    void update(T entity);
    void delete(String id);
    
    // Additional methods can be added as needed for specific entity types
}