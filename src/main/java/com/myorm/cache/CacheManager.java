package com.myorm.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private static final CacheManager instance = new CacheManager();
    
    private final Cache<CacheKey, Object> cache;

    private CacheManager() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }

    public static CacheManager getInstance() {
        return instance;
    }

    public <T> void put(Class<T> entityClass, Object id, T entity) {
        cache.put(new CacheKey(entityClass, id), entity);
        System.out.println("🔵 Cache: Stored " + entityClass.getSimpleName() + " with ID " + id);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> entityClass, Object id) {
        T result = (T) cache.getIfPresent(new CacheKey(entityClass, id));
        if (result != null) {
            System.out.println("🟢 Cache: Hit for " + entityClass.getSimpleName() + " with ID " + id);
        } else {
            System.out.println("🔴 Cache: Miss for " + entityClass.getSimpleName() + " with ID " + id);
        }
        return result;
    }

    public void invalidate(Class<?> entityClass, Object id) {
        cache.invalidate(new CacheKey(entityClass, id));
        System.out.println("🗑️ Cache: Invalidated " + entityClass.getSimpleName() + " with ID " + id);
    }

    public void clear() {
        cache.invalidateAll();
        System.out.println("🧹 Cache: Cleared all entries");
    }

    public String getStats() {
        return cache.stats().toString();
    }
} 