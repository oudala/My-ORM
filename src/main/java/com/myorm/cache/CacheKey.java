package com.myorm.cache;

import java.util.Objects;

public class CacheKey {
    private final Class<?> entityClass;
    private final Object id;

    public CacheKey(Class<?> entityClass, Object id) {
        this.entityClass = entityClass;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(entityClass, cacheKey.entityClass) &&
               Objects.equals(id, cacheKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityClass, id);
    }
} 