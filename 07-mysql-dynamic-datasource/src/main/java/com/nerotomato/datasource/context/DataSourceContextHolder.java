package com.nerotomato.datasource.context;

import com.nerotomato.datasource.type.DynamicDataSource;

import java.util.Objects;

/**
 * Created by nero on 2021/5/19.
 */
public class DataSourceContextHolder {
    private static final ThreadLocal<DynamicDataSource> context = new ThreadLocal<>();

    public static void set(DynamicDataSource dynamicDataSource) {
        context.set(Objects.requireNonNull(dynamicDataSource, "dynamicDataSource can not be null!"));
    }

    public static DynamicDataSource getDynamicDataSource() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
